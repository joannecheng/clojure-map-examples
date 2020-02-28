(ns clojure-map-examples.raster-examples
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
                               add_shadow
                               add_water detect_water]]
           '[base :refer [summary]]
           '[raster :refer [extract raster]])

;; http://viewfinderpanoramas.org/dem3.html#nam

; TODO: extract elevation values (in meters) from a raster file
;; Extract raster data

(def output-location (str (System/getProperty "user.dir") "/resources/output"))
(def load-raster (r/r "function(f) raster(f)"))
(def clear-rgl (r/r "rgl::rgl.clear"))
(def rgl-snapshot (r/r "rgl::rgl.snapshot"))

(def rmnp_dem
  (load-raster (-> (io/resource "data_files/rmnp_dem.tif") io/file str)))

(def rmnp-dem-matrix
  (-> rmnp_dem
      (raster_to_matrix)
      #_(reduce_matrix_size 0.5)))

;; simple example
(-> rmnp-dem-matrix
    (sphere_shade)
    plot_map)

;; 'add water'
(-> rmnp-dem-matrix
    sphere_shade
    (add_water (detect_water rmnp-dem-matrix :min_area 250) :color "steelblue")
    plot_map)

;; 'add water'
(-> rmnp-dem-matrix
    (sphere_shade :texture "imhof4")
    (add_water (detect_water rmnp-dem-matrix :min_area 250) :color "steelblue")
    plot_map)

;; using lamb_shade
(-> rmnp-dem-matrix
    (lamb_shade :zscale 33)
    plot_map)

;; adding shadow
(-> rmnp-dem-matrix
    sphere_shade
    (add_shadow (ray_shade rmnp-dem-matrix :zscale 23 :sunaltitude 3 :lambert false) :max_darken 0.5)
    (add_shadow (lamb_shade rmnp-dem-matrix :zscale 23 :sunaltitude 3) :max_darken 0.5)
    plot_map)

;; Render 3D
(-> rmnp-dem-matrix
    sphere_shade
    (plot_3d rmnp-dem-matrix :zscale 10))

;; Render 3D with lots of fancy options to focus on lakes
(clear-rgl)
(def ambientshadows (ambient_shade rmnp-dem-matrix))
(-> rmnp-dem-matrix
    sphere_shade
    (add_water (detect_water rmnp-dem-matrix :min_area 270) :color "steelblue")
    (add_shadow (ray_shade rmnp-dem-matrix :sunangle 200 :sunaltitude 5 :zscale 33 :lambert false) :max_darken 0.5)
    (add_shadow (lamb_shade rmnp-dem-matrix :sunangle 190 :sunaltitude 3 :zscale 33) :max_darken 0.7)
    (add_shadow ambientshadows :max_darken 0.2)
    (plot_3d rmnp-dem-matrix :zscale 15))

(render_camera :theta 315 :zoom 0.8 :phi 30)
(render_camera :theta 315 :phi 60 :zoom 0.99)

(render_depth :focus 0.19 :focallength 40 :filename "test-3d.png")

;; Focus longs peak
(render_camera :theta 35 :zoom 0.15 :phi 8)
(render_depth :focus 0.31 :focallength 40 :filename (str output-location "/test-3d.png"))

(for [x (range 10 100 5)]
  (render_depth :focus 0.19 :focallength x :filename (str output-location "/focal-" x ".png")))

(for [x (range 0.1 0.8 0.02)]
  (render_depth :focus x :focallength 30 :filename (str output-location "/focus-" (format "%.2f" x) ".png")))

(for [x (range 0 360 5)]
  (do
    (render_camera :theta x :phi 30)
    (rgl-snapshot (str output-location "/theta-" (format "%03d" x) ".png") :fmt "png" :top true)))

(for [x (range 0 90 5)]
  (do
    (render_camera :phi x)
    (rgl-snapshot (str output-location "/phi-" (format "%02d" x) ".png") :fmt "png" :top true)))

;; TODO Render 3D with lots of fancy options to focus on Longs Peak

;; add_water(detect_water(rmnp_mat), color = "lightblue") %>%
;; add_shadow(ray_shade(rmnp_mat, sunangle = 190, sunaltitude = 3, zscale = 33, lambert = FALSE), max_darken = 0.5) %>%
;; add_shadow(lamb_shade(rmnp_mat, sunangle = 190, sunaltitude = 3, zscale = 33), max_darken = 0.5) %>%
;; render_camera(theta = 315, phi = 8, zoom = 0.1, fov = 90)
;; render_depth(focus = 0.56, focallength = 100)
