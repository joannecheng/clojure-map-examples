(ns clojure-map-examples.denver-berlin
  (:require [clojisr.v1.r :as r]
            [clojisr.v1.require :refer [require-r]]))

(require-r '[maps :as maps]
           '[geosphere :as geosphere :refer [greatCircle]])

;; rewriting this so we don't override clojure's very very important
;; "map" function

(defn great-circle [loc1, loc2]
  ((r/r "function(userLL,relationLL){
                                   tmpCircle = greatCircle(userLL,relationLL, n=200)
                                   start = which.min(abs(tmpCircle[,1] - data.frame(userLL)[1,1]))
                                   end = which.min(abs(tmpCircle[,1] - relationLL[1]))
                                   greatC = tmpCircle[start:end,]
                                   return(greatC)}")
        loc1 loc2))

(def world-map (r/r "maps::map"))
(def berlin [13.4050 52.5200])
(def denver [-104.9903 39.7392])

(world-map "world" :col "#d2d2d2" :fill true :bg "white"
           :lwd 0.05 :ylim [-20 80] :xlim [-140 40])
((r/r "lines") (great-circle denver berlin) :col "#c05137" :lwd 3)

;;# No margin
;;par(mar=c(0,0,0,0))
