(ns pokeplayground.logic)

(defn calculate-evolutions
  "Gets all evolutions details from an evolution-chain structure"
  [{:keys [evolves_to species is_baby]}]
  (if (nil? species)
    []
    (concat [{:pokemon/species-url (:url species) :pokemon/name (:name species) :pokemon/is-baby? is_baby}]
            (flatten (map calculate-evolutions evolves_to)))))
