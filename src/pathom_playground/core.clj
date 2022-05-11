(ns pathom-playground.core
  (:require [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.connect :as pc]
            [pokeplayground.resolvers :as poke-resolvers]))

;; Create a parser that uses the resolvers:
(def parser
  (p/parser
    {::p/env     {::p/reader               [p/map-reader
                                            pc/reader2
                                            pc/open-ident-reader
                                            p/env-placeholder-reader]
                  ::p/placeholder-prefixes #{">"}}
     ::p/mutate  pc/mutate
     ::p/plugins [(pc/connect-plugin {::pc/register poke-resolvers/registry}) ;setup connect and use our resolvers
                  p/error-handler-plugin
                  p/trace-plugin]}))

(comment
  ; to call the parser and get some data out of it, run:
  (parser {} [{[:pokemon/name "charmander"] [:pokemon/id {:pokemon/evolves-to [:pokemon/name]}]}])
  (parser {} [{[:pokemon/name "mankey"] [:pokemon/id {:pokemon/evolves-to [:pokemon/name :pokemon/species-url]}
                                         :pokemon/id {:pokemon/evolution-chain [:url]}
                                         :pokemon/id {:pokemon/types [:type/url :type/name]}]}])
  (parser {} [{[:pokemon/name "mankey"] [:pokemon/id {:pokemon/evolves-to [:pokemon/name :pokemon/id]}
                                         :pokemon/id {:pokemon/evolution-chain [:url]}
                                         :pokemon/id {:pokemon/types [:type/url :type/name]}]}]))