;;lein test :only datomic-dsl.add-test
(ns datomic-dsl.add-test
  (:require [clojure.test :refer :all]
            [datomic-dsl.add :refer :all]
            [datomic-dsl.common-test :refer :all]
            [datomic-dsl.create-test :refer :all]
            [clojure.pprint :refer :all]
            [datomic.api :as d]))

(def book-add
  "Define data sturct and convert it to Datomic syntax"
  (add-datom "book"
             {"title" "title"
             "author" "author"}))

;; (pprint book-add)

(def add-result
  "Keep a reference to the transaction result if we need to look at it later."
  (d/transact conn book-add))

(def book-datom
  "Qurey against the result. Full sotp in the query to find the first one."
  (d/touch
   (d/entity
    (d/db conn)
    (d/q '[:find ?e . :where [?e :book/author]]
         (d/db conn)))))

;; (pprint book-datom)

(deftest datom-added-test
  (testing "Is the datom in the database?"
    (is (= 2 (count book/datom)))))
