(ns pokeplayground.resolvers
  (:require [com.wsscode.pathom.connect :as pc]
            [pokeplayground.logic :as logic]
            [pathom-playground.utils :as utils]))

(defn party [] (atom []))

(pc/defresolver pokemon-infos-by-name [_ {:pokemon/keys [name]}]
                {::pc/input  #{:pokemon/name}
                 ::pc/output [:pokemon/infos]}
                (let [pokemon-infos (utils/http-get (str "https://pokeapi.co/api/v2/pokemon/" name))]
                  {:pokemon/infos pokemon-infos}))

(pc/defresolver pokemon-infos-by-id [_ {:pokemon/keys [id]}]
  {::pc/input  #{:pokemon/id}
   ::pc/output [:pokemon/infos]}
  (let [pokemon-infos (utils/http-get (str "https://pokeapi.co/api/v2/pokemon/" id))]
    {:pokemon/infos pokemon-infos}))

(pc/defresolver pokemon [_ {:pokemon/keys [infos]}]
                {::pc/input  #{:pokemon/infos}
                 ::pc/output [:pokemon/id :pokemon/name]}
                {:pokemon/id (get-in infos [:id])
                 :pokemon/name (get-in infos [:name])})

(pc/defresolver pokemon-types [_ {:pokemon/keys [infos]}]
                {::pc/input  #{:pokemon/infos}
                 ::pc/output [{:pokemon/types
                               {:type/url
                                [:type/url :type/name]}}]}
                (let [types (map (comp (partial utils/namespaced "type") :type) (:types infos))]
                  {:pokemon/types types}))

(pc/defresolver pokemon-evolution-chain [_ {:pokemon/keys [id]}]
                {::pc/input  #{:pokemon/id}
                 ::pc/output [{:pokemon/evolution-chain [:url] }]}
                (let [pokemon-species-info (utils/http-get (str "https://pokeapi.co/api/v2/pokemon-species/" id))]
                  {:pokemon/evolution-chain {:url (get-in pokemon-species-info [:evolution_chain :url])}}))


(pc/defresolver pokemon-evolutions [_ {:pokemon/keys [evolution-chain]}]
                {::pc/input  #{:pokemon/evolution-chain}
                 ::pc/output [{:pokemon/evolves-to
                               {:pokemon/species-url
                                [:pokemon/species-url :pokemon/name :pokemon/is-baby?]}}]}
                (let [pokemon-evolution-chain (-> (utils/http-get (:url evolution-chain))
                                                  (get-in [:chain]))
                      evolutions          (logic/calculate-evolutions pokemon-evolution-chain)]
                  {:pokemon/evolves-to (vec evolutions)}))

(pc/defresolver my-party [{:keys [db]} _]
  {::pc/input  #{}
   ::pc/output [{:my.party/pokemons
                 {:pokemon/id
                  [:pokemon/id]}}]}
  {:my.party/pokemons @db})

(pc/defmutation add-to-party [{:keys [db] :as env} {:pokemon/keys [id]}]
                {::pc/sym    'add-to-party
                 ::pc/params [:pokemon/id]
                 ::pc/output [{:my.party/pokemons
                               {:pokemon/id
                                [:pokemon/id]}}]}
      {:my.party/pokemons (swap! db conj {:pokemon/id id})})

; resolvers are just maps, we can compose many using sequences
(def registry
  [pokemon-infos-by-id pokemon-infos-by-name pokemon pokemon-types pokemon-evolution-chain pokemon-evolutions add-to-party my-party])
