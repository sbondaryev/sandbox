(ns sandbox.chain.operations)

(def v1
  {:title "Pianocat Plays Carnegie Hall"
   :type :cat
   :length 300})

(def v2
  {:title "Paint Drying"
   :type :home-improvement
   :length 600})

(def v3
  {:title "Fuzzy McMittens Live At The Apollo"
   :type :cat
   :length 200})

(def videos [v1 v2 v3])

(defn cat-time [videos]
  (apply +
         (map :length
              (filter (fn [video] (= :cat (:type video))) videos))))

(defn more-cat-time [videos]
  (->> videos
       (filter (fn [video] (= :cat (:type video))))
       (map :length)
       (apply +)))
