(ns clojure-map-examples.black-canyon
  (:require [clojisr.v1.r :as r]
            [clojisr.v1.require :refer [require-r]]
            [clojisr.v1.rserve :as rserve]
            [clojure.java.io :as io]))

(rserve/set-as-default!)
(r/discard-all-sessions)

(require-r '[rayshader :refer [raster_to_matrix reduce_matrix_size
                               render_camera render_depth
                               ray_shade sphere_shade lamb_shade ambient_shade ;; shaders
                               plot_map plot_3d ;; plotting functions
                               add_shadow add_overlay
                               add_water detect_water]]
           '[base :refer [summary]]
           '[tiff :refer [readTIFF]]
           '[raster :refer [extract raster]])

(def load-raster (r/r "function(f) raster(f)"))
(def clear-rgl (r/r "rgl::rgl.clear"))
(def rgl-snapshot (r/r "rgl::rgl.snapshot"))

(def output-location (str (System/getProperty "user.dir") "/resources/output"))

(def black-canyon-dem
  (load-raster (-> (io/resource "data_files/black-canyon-output1.tif") io/file str)))

;; (def black-canyon-texture (readTIFF (-> (io/resource "data_files/black-canyon-output1.tif") io/file str)))
;; FIXME: overlay/texture isn't working now

(def bc-dem-matrix
  (-> black-canyon-dem
      (raster_to_matrix)
      (reduce_matrix_size 0.5)))

(clear-rgl)
(def ambientshadows (ambient_shade bc-dem-matrix))
(-> bc-dem-matrix
    (sphere_shade :texture "desert")
    (add_water (detect_water bc-dem-matrix :min_area 270) :color "steelblue")
    (add_shadow (ray_shade bc-dem-matrix :sunangle 200 :sunaltitude 5 :zscale 33 :lambert false) :max_darken 0.5)
    (add_shadow (lamb_shade bc-dem-matrix :sunangle 190 :sunaltitude 7 :zscale 33) :max_darken 0.7)
    (add_shadow ambientshadows :max_darken 0.2)
    (plot_3d bc-dem-matrix :zscale 30 :windowsize 1200))

(render_camera :theta 29 :phi 30 :zoom 0.8)

(for [x (range 0 360 5)]
  (do
    (render_camera :theta x :phi 30)
    (rgl-snapshot (str output-location "/theta-" (format "%03d" x) ".png") :fmt "png" :top true)))
(rgl-snapshot :filename "/home/joannecheng/blackcanyon.png")
