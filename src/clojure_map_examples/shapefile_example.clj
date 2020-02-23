(ns clojure-map-examples.shapefile-example
  (:require [clojisr.v1.r :as r]
            [clojisr.v1.require :refer [require-r]]
            [clojure.java.io :as io]))

(require-r '[ggplot2 :refer [ggplot aes theme_void ;; general plotting
                             map_data ;; get base map for area (ex: Colorado, Germany)
                             coord_sf ;; takes a projection from the first shapefile in plot and applies it to the map
                             coord_map ;; set map projection
                             ggsave ;; save plot
                             geom_sf geom_polygon]] ;; plotting data
           '[sf :refer [st_bbox st_crop st_read]])


;; TODO move to resources
(def data-location  "/home/joannecheng/dev/viz/clojure-rayshader/resources/data_files")
(def output-location (str (System/getProperty "user.dir") "/resources/output"))

(def lakes (-> data-location
             (str "/lakes_in_colorado_shp/geo_export_1d691995-66a3-4b66-8aa2-4ae94088e589.shp")
             st_read))

(def colorado-base (map_data "state" :region "Colorado"))
(def germany-base (map_data "world" :region "Germany"))

;; Plot Germany
(def germany-plot
  (-> (ggplot)
      (r/r+ (geom_polygon :data germany-base (aes :x 'long :y 'lat :group 'group)
                          :fill "#000000")
            (coord_map)
            (theme_void))))

;; Save to PDF
(ggsave :file (str output-location "/germany-test.pdf")
        :plot germany-plot)

;; TODO filter lakes with names
(defn lakes-with-names [lakes]
  )

(def lake-blue "#3e98ed")
(def grey "#555555")
(def map-base "#f6f6f5")

;; Plot all lakes
(-> (ggplot)
    (r/r+ (geom_polygon :data colorado-base (aes :x 'long :y 'lat :group 'group)
                        :color "#000000" :fill map-base :size 0.2)
          (geom_sf :data lakes :size 0.2 :color lake-blue :fill lake-blue (aes :geometry 'geometry))
          ;;geom_sf(data=lakes_with_names, aes(geometry=geometry), color=NA, fill="#2c7bb6") +
          (coord_sf)
          (theme_void)))

;; TODO Plot lakes with names, without names
;; TODO Plot lakes in order by size, filter by water with the name "lake"

;; DONE Filter by Area

(defn read-shapefile-resource [sf-resource]
  (-> sf-resource
      io/file
      str
      st_read))

;; DONE Get RMNP shapefile
(def rmnp-sf-file (io/resource "data_files/RMNP/Rocky_Mountain_National_Park__Boundary_Polygon.shp"))
(def rmnp-trails-sf-file (io/resource "data_files/RMNP/Rocky_Mountain_National_Park__Trails.shp"))
(def rmnp-sf (read-shapefile-resource rmnp-sf-file))
(def rmnp-trails-sf (read-shapefile-resource rmnp-trails-sf-file))

;; Get BBox
(def rmnp-bbox
  (interleave
   [:xmin :ymin :xmax :ymax]
   (r/r->clj
    (st_bbox rmnp-sf))))

(def rmnp-lakes (apply (r/r "st_crop") lakes rmnp-bbox))

;; Only plot lakes in RMNP
(-> (ggplot)
    (r/r+ (geom_sf :data rmnp-sf (aes :geometry 'geometry) :size 0.1)
          (geom_sf :data rmnp-lakes (aes :geometry 'geometry) :size 0.1 :fill lake-blue)
          (geom_sf :data rmnp-trails-sf (aes :geometry 'geometry) :size 0.3)
          (theme_void)))

;; TODO STYLE THE SHIT OUT OF THIS STUPID MAP

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RASTER EXAMPLES
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; TODO: Pull out to separate file!

(require-r '[rayshader :refer [raster_to_matrix  reduce_matrix_size sphere_shade plot_map]]
           '[raster :refer [extract raster]])

# These lines extract elevation values (in meters) from a raster file
; DONE colorado_raster = raster::raster(paste(dloc, "/colorado_dem_clipped.tif", sep=""))
; p <- st_centroid(lakes["geometry"])
; lakes$elevation <- raster::extract(colorado_raster, p)

;; Extract raster data
(def colorado_dem
  ;; This is a HACK - I need to wrap 'raster::raster' around a function because
  ;; R keeps calling the wrong 'raster' and is unable to find a "default function"
  ((r/r "function(f) raster(f)")
   (str data-location "/colorado_dem_clipped.tif")))

(def colorado-dem-matrix
  (-> colorado_dem
      (raster_to_matrix)
      (reduce_matrix_size 0.5)))

(-> colorado-dem-matrix
    sphere_shade
    plot_map)


