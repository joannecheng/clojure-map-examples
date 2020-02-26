(ns clojure-map-examples.raster-examples
  (:require [clojisr.v1.r :as r]
            [clojisr.v1.require :refer [require-r]]
            [clojure.java.io :as io]))

(require-r '[rayshader :refer [raster_to_matrix  reduce_matrix_size plot_map
                               ray_shade sphere_shade lamb_shade ;; shaders
                               add_shadow
                               add_water detect_water]]
           '[raster :refer [extract raster]])

;; http://viewfinderpanoramas.org/dem3.html#nam

; TODO: extract elevation values (in meters) from a raster file
;; Extract raster data

(def load-raster (r/r "function(f) raster(f)"))

(def rmnp_dem
  (load-raster (-> (io/resource "data_files/rmnp_dem.tif") io/file str)))

(def rmnp-dem-matrix
  (-> rmnp_dem
      (raster_to_matrix)
      #_(reduce_matrix_size 0.5)))

;; simple example
(-> rmnp-dem-matrix
    sphere_shade
    plot_map)

;; 'add water'
(-> rmnp-dem-matrix
    sphere_shade
    (add_water (detect_water rmnp-dem-matrix) :color "steelblue")
    plot_map)

;; using lamb_shade
(-> rmnp-dem-matrix
    (lamb_shade :zscale 33)
    plot_map)

;; adding shadow
(-> rmnp-dem-matrix
    (lamb_shade :zscale 18)
    (add_shadow (ray_shade rmnp-dem-matrix :zscale 33 :sunaltiude 6 :lambert false) 0.3)
    plot_map)