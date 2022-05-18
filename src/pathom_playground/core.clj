(ns pathom-playground.core
  (:require [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.viz.ws-connector.core :as p.connector]
            [com.wsscode.pathom.connect :as pc]
            [pokeplayground.resolvers :as poke-resolvers]))

(def CONNECT_PARSER? false)

;; Create a parser that uses the resolvers:
(def parser
  (cond->> (p/parser
             {::p/env     {::p/reader               [p/map-reader
                                                     pc/reader2
                                                     pc/open-ident-reader
                                                     p/env-placeholder-reader]
                           ::p/placeholder-prefixes #{">"}
                           :db                     (poke-resolvers/party)}
              ::p/mutate  pc/mutate
              ::p/plugins [(pc/connect-plugin {::pc/register poke-resolvers/registry}) ;setup connect and use our resolvers
                           p/error-handler-plugin
                           p/trace-plugin]})
           CONNECT_PARSER?
           (p.connector/connect-parser
             ; parser-id is optional, but highly encouraged, without this
             ; the application can't know about the parser identity and will not
             ; be able to remember data about query history across parser connections
             {::p.connector/parser-id ::poke-parser})))

(comment
  ; to call the parser and get some data out of it, run:
  (parser {} [{[:pokemon/name "charmander"] [:pokemon/id {:pokemon/evolves-to [:pokemon/name]}]}])
  (parser {} [{[:pokemon/name "charmander"] [:pokemon/id {:pokemon/evolves-to [:pokemon/name]}]}
              {[:pokemon/name "pikachu"] [:pokemon/id {:pokemon/evolves-to [:pokemon/name]}]}])
  (parser {} [{[:pokemon/name "mankey"] [:pokemon/id {:pokemon/evolves-to [:pokemon/name :pokemon/species-url]}
                                         :pokemon/id {:pokemon/evolution-chain [:url]}
                                         :pokemon/id {:pokemon/types [:type/url :type/name]}]}])
  (parser {} [{[:pokemon/name "mankey"] [:pokemon/id {:pokemon/evolves-to [:pokemon/name :pokemon/id]}
                                         :pokemon/id {:pokemon/evolution-chain [:url]}
                                         :pokemon/id {:pokemon/types [:type/url :type/name]}]}])
  (parser {} '[(add-to-party {:pokemon/id 4})])
  (parser {} [:my.party/pokemons])
  (parser {} [{:my.party/pokemons [:pokemon/id :pokemon/name]}])
  (parser {} '[{(add-to-party {:pokemon/id 3}) [{:my.party/pokemons [:pokemon/id :pokemon/name]}]}]))
