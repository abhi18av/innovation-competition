(ns innovation-competition.scratch
  (:require [clojure.edn :as edn]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reading data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(comment
  (take 5 ideas)
  (count ideas))

(def ideas
  (edn/read-string
   (slurp "./resources/ideas.edn")))


;;;;;;;;;;;;;;;;;


(comment
  (take 5 ideas)
  (count ideas))

(def users
  (edn/read-string
   (slurp "./resources/users.edn")))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Exploring the users data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Check for regularity of data

;; TODO Check for regularity of keys in the data

;; TODO Collect all (un)common keys in the data

(comment)



;; DONE Check for empty values in the data

(comment
 (empty-keys-in-data? ideas)
 (empty-keys-in-data? users))

;; FIXME
;; remove the hard-coded keys
;; should return a flattened-hash-map rather than the current nested-one
(defn empty-keys-in-data? [data]
  (let [all-keys #_(keys (first data)) [:fname :lname :email :id :house]]
    (for [a-key all-keys]
      (let [count-of-empty-keys (count
                                 (filter nil? (map a-key users)))]
          (if
              (not= 0 count-of-empty-keys)
            {a-key true}
            {a-key false})))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Exploring the users data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; There are authors with multiple ideas
(=
 (count
  (map :author-id ideas))

 (count
  (into #{}
        (map :author-id ideas))))

;; NOTE people have submitted multiple ideas

(take 10
      (frequencies
       (map :author-id ideas)))

;; TODO


(def frequencies-of-keys [])

;;;;;;;;;;;;;;;;;


(keys (first users))

(count
 (map :id users))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utility functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(comment
  (average-score (range 10)))

(defn average-score
  [scores]
  (double
   (if (empty? scores)
     0
     (/ (reduce + scores) (count scores)))))

;;;;;;;;;;;;;;;;;


(comment
  (find-average-score-of-idea  (first ideas))
  (map find-average-score-of-idea (take  5 ideas)))

;; TODO store this key in the main users itself
(defn find-average-score-of-idea [an-idea]
  (average-score
   (filter
    (fn [x]
      (not (nil? x)))
    (:scores  an-idea))))

(def ideas-with-average-scores
 (map
  (fn [an-idea]
    (assoc an-idea :scores (find-average-score-of-idea an-idea)))
  ideas))

;;(clojure.pprint/pprint ideas-with-average-scores (clojure.java.io/writer "ideas-final.edn"))

;;;;;;;;;;;;;;;;;


(comment
 (take 10 users-with-houses))


(def users-with-houses
  (map
   (fn [a-user]
     (assoc a-user :house (:house a-user "Free Folk")))
   users))

;;;;;;;;;;;;;;;;;
;; DONE Separate the user houses

(comment
  (make-unique-houses (nth users-with-houses 0))
  (make-unique-houses (nth users-with-houses 8)))


(defn make-unique-houses [x]
  (cond
    (string? (:house x)) x

    (set? (:house x)) (let [double-houses (into [] (:house x))]
                        (for [a-house double-houses]
                          (assoc x :house a-house)))))

(def users-final
  (flatten (map make-unique-houses users-with-houses)))


;;(clojure.pprint/pprint users-final (clojure.java.io/writer "users-final-pprint.edn"))

;;;;;;;;;;;;;;;;;

(filter
 (fn [x] (= x "Free Idea"))
 (map
  (fn [an-idea]
    (:author-id an-idea "Free Idea"))
  ideas))

;;;;;;;;;;;;;;;;;
(comment
  (user-info "user-53-0008852"))

;; TODO add edge case of user-not-present
(defn user-info [a-user-id]
  (filter
   (fn [x]
     (= a-user-id (:id x)))
   users))


;;;;;;;;;;;;;;;;;

;; TODO Can drop the <id> from :author-info since now it's repetitive


(defn join-idea-and-author [an-idea]
  (let [author-id (:author-id an-idea)]
    {:idea an-idea
     :authorship (user-info author-id)}))

(map join-idea-and-author
     (take 10 ideas))


;;;;;;;;;;;;;;;;;
;; TODO filter ideas from the stark house


(def joined-data (map join-idea-and-author
                      (take 10 ideas)))

(filter
 (fn [x]
   (user-belongs-to-house-in-joined-data? "Martell" x))
 joined-data)

