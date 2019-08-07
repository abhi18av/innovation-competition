(ns scratch.core
  (:require [clojure.pprint :as pprint]))

(def data
  [{:house-name "Martell", :innovation-score 5.013253012048194}
   {:house-name "FreeFolk", :innovation-score 4.784715821812596}
   {:house-name "Baratheon", :innovation-score 4.729347826086958}
   {:house-name "Stark", :innovation-score 4.7155844155844155}
   {:house-name "Tully", :innovation-score 4.6969879518072295}
   {:house-name "Lannister", :innovation-score 4.599253731343283}
   {:house-name "Tyrell", :innovation-score 4.583943089430894}
   {:house-name "Targaryen", :innovation-score 4.529215686274512}
   {:house-name "Greyjoy", :innovation-score 4.512500000000001}])

(def ascii-colors
  {:red "\u001b[31m"
   :green "\u001b[32m"
   :yellow "\u001b[33m"
   :blue "\u001b[34m"
   :magenta "\u001b[35m"
   :cyan "\u001b[36m"
   :white "\u001b[37m"
   :reset "\u001b[0m"})

(defn colorized-output [color text]
  (with-out-str
    (js/console.log "\n##########\n"
                    (str ((keyword color) ascii-colors)
                         text
                         (:reset ascii-colors))
                    "\n##########\n")))

(colorized-output "cyan" "The output")

(js/console.log (clj->js data))


;; (colorized-output "yellow"
;;                   (clojure.pprint/pprint-table data))
