(ns clojure-map-examples.shapefile-example
  (:require [clojisr.v1.r :as r]
            [clojisr.v1.require :refer [require-r]]
            [clojure.java.io :as io]))

(require-r '[ggplot2 :as ggplot2 :refer [ggplot aes theme_void ;; general plotting
                                         map_data ;; get base map for area (ex: Colorado, Germany)
                                         coord_sf ;; takes a projection from the first shapefile in plot and applies it to the map
                                         coord_map ;; set map projection
                                         ggsave ;; save plot
                                         geom_sf geom_polygon]] ;; plotting data
           '[base :as base :refer [$ summary]]
           '[sf :refer [st_bbox st_crop st_read]])

(defn read-shapefile-resource [sf-resource]
  (-> sf-resource
      io/file
      str
      st_read))

(def output-location (str (System/getProperty "user.dir") "/resources/output"))

(require-r '[ggplot2 :refer [map_data]])
(def colorado-base (map_data "state" :region "Colorado"))
(summary colorado-base)

(def germany-base (map_data "world" :region "Germany"))

;; Plot Germany
(def germany-plot
  (-> (ggplot)
      (r/r+ (geom_polygon :data germany-base (aes :x 'long :y 'lat :group 'group)
                          :fill "#90EE90" :color "#000000" :size 0.4)
            (coord_map) ;; projection
            (theme_void))))
germany-plot
(r/r "X11()")

\;; Plot Colorado
(-> (ggplot) ;; initializes a 'ggplot' object, the 'base' of the graph
    (r/r+ (geom_polygon :data colorado-base
                        ;; passing in the data (collection of lat/long coordinates)
                        ;; A reminder - colorado-base is a dataframe

                        (aes :x 'long :y 'lat :group 'group)
                        ;; 'aesthetic mapping' - describes how the
                        ;; incoming data is mapped to visual properties
                        ;; in this case, the 'long' column is mapped to the x axis,
                        ;; the 'lat' column is mapped to the y axis

                        :fill "#eeeeee" :color "#000000" :size 0.2) ;; styling
          (coord_map) ;; Sets the projection. Default is mercator
          (theme_void))) ;; removes axis lines from the chart

(def colorado-water (-> "data_files/lakes/geo_export_1d691995-66a3-4b66-8aa2-4ae94088e589.shp"
                        io/resource
                        read-shapefile-resource))

;; Quick summary of colorado-water
(summary colorado-water)
;; Get a column of a dataframe
($ colorado-water 'name)
;; turn it into a clojure list for easier inspection
(r/r->clj ($ colorado-water 'name))

;; Filter bodies of water with names
(def colorado-water-with-names
  (r/bra ;; basically []
   colorado-water
   (base/! (base/is-na ($ colorado-water 'name)))
   r/empty-symbol)) ;; ,

;;(def lake-blue "#3e98ed")
(def lake-blue "#3187b1")
(def grey "#555555")
(def map-base "#f6f6f5")

;; Plot all colorado water
(defn colorado-water-plot []
  (-> (ggplot)
      (r/r+ (geom_polygon :data colorado-base (aes :x 'long :y 'lat :group 'group)
                          :color "#000000" :fill map-base :size 0.2)
            (geom_sf :data colorado-water :size 0.1 :color "#111111" :fill lake-blue (aes :geometry 'geometry))
            ;;geom_sf(data=colorado-water_with_names, aes(geometry=geometry), color=NA, fill="#2c7bb6") +
            (coord_sf)
            (theme_void))))
(colorado-water-plot)

(defn colorado-filtered []
  (-> (ggplot)
      (r/r+ (geom_polygon :data colorado-base (aes :x 'long :y 'lat :group 'group)
                          :color "#000000" :fill map-base :size 0.2)
            (geom_sf :data colorado-water :size 0.05 :color "#111111cc" :fill "#cccccccc" (aes :geometry 'geometry))
            (geom_sf :data colorado-water-with-names (aes :geometry 'geometry) :color "#111111" :fill lake-blue
                     :size 0.05)
            (coord_sf)
            (theme_void))))
(colorado-filtered)

;; Save to file
(ggsave :plot (colorado-filtered)
        :file (str output-location "/colorado-water-filtered-output.png")
        :height 12 :width 16)


;; Get RMNP shapefile
(def rmnp-sf-file (io/resource "data_files/RMNP/Rocky_Mountain_National_Park__Boundary_Polygon.shp"))
(def rmnp-trails-sf-file (io/resource "data_files/RMNP/Rocky_Mountain_National_Park__Trails.shp"))
(def rmnp-sf (read-shapefile-resource rmnp-sf-file))
(def rmnp-trails-sf (read-shapefile-resource rmnp-trails-sf-file))

;; Filter by Area
;; Get BBox
(def rmnp-bbox
  (interleave
   [:xmin :ymin :xmax :ymax]
   (r/r->clj
    (st_bbox rmnp-sf))))

(def rmnp-colorado-water (apply st_crop colorado-water rmnp-bbox))

;; Adding More shapefiles - trails, park boundary
(def park-green "#a7b996aa")
(-> (ggplot)
    (r/r+ (geom_sf :data rmnp-sf (aes :geometry 'geometry) :size 0.1 :fill park-green)
          (geom_sf :data rmnp-colorado-water (aes :geometry 'geometry) :size 0.1 :fill lake-blue)
          (geom_sf :data rmnp-trails-sf (aes :geometry 'geometry) :size 0.2)
          (theme_void)))
