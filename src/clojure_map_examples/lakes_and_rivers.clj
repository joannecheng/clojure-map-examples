(ns clojure-map-examples.lakes-and-rivers
  (:require [clojisr.v1.r :as r]
            [clojisr.v1.require :refer [require-r]]
            [clojisr.v1.rserve :as rserve]
            [clojure.java.io :as io]))

(require-r '[sf :as sf :refer [st_read]]
           '[ggplot2 :as ggplot2 :refer [ggplot aes geom_polygon theme_void
                                         coord_sf ;; takes a projection from the first shapefile in plot and applies it to the map
                                         geom_sf]]
           '[utils :as utils]
           '[rmapshaper :as rmapshaper]
           '[base :as base :refer [$ summary]])

(defn read-shapefile-resource [sf-resource]
  (-> sf-resource
      io/file
      str
      st_read))

(def colorado-water
  (-> "data_files/lakes/geo_export_1d691995-66a3-4b66-8aa2-4ae94088e589.shp"
      io/resource
      read-shapefile-resource))

(defn water-with-names [df]
  (r/bra
   df
   (base/! (base/is-na ($ df 'name)))
   nil))

(defn water-without-names [df]
  ;; Think of ($ dataframe 'col-name) as
  ;; (get map-object :key-name)
  (r/bra df (base/is-na ($ df 'name)) nil))

(defn state-basemap [state]
  (geom_polygon :data (ggplot2/map_data "state" :region state)
                (aes :x 'long :y 'lat :group 'group)
                :fill "#F7F5F088" :color "#333333" :size 0.2))

#_(r/r "X11()")
(r/r "X11(display=\"\", width=12, height=10)")
(r/r "dev.off()")

(-> (ggplot)
    (r/r+ (state-basemap "colorado")
          (geom_sf :data (water-with-names colorado-water) (aes :geometry 'geometry)
                   :fill "#72bfcf" :size 0.1)
          (geom_sf :data (water-without-names colorado-water) (aes :geometry 'geometry)
                   :fill "#333333" :size 0.1
                   :color "#274a51")

          (coord_sf)
          (theme_void)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; New Jersey

;; Open this file in QGIS to get a good overview

; Load water data
(def new-jersey-water
  (-> "data_files/new_jersey/NHDWaterbody_2002_shapefile/NHDWaterbody_2002.shp"
      io/resource
      read-shapefile-resource))

; What are the columns of data we can find here?
(base/colnames new-jersey-water)
;; Sometimes i think it's easier to deal with clojure types
;; so I can interact with the repl easier
(r/r->clj (base/colnames new-jersey-water))
;; [defun to comment]
;; => ["COMID"
;;     "PERMANENT_"
;;     "FDATE"
;;     "RESOLUTION"
;;     "GNIS_ID"
;;     "GNIS_NAME"
;;     "AREASQKM"
;;     "ELEVATION"
;;     "REACHCODE"
;;     "FTYPE"
;;     "FCODE"
;;     "FTYPE_DESC"
;;     "FCODE_DESC"
;;     "SHAPE_Leng"
;;     "SHAPE_Area"
;;     "geometry"]

;; FTYPE_DESC is the column we want

;; What are the types of water?
(-> ($ new-jersey-water 'FTYPE_DESC)
    r/r->clj
    distinct)
;; => ("Canal/Ditch"
;;     "Stream/River"
;;     "Sea/Ocean"
;;     "Spillway"
;;     "Lake/Pond"
;;     "Reservoir"
;;     "Estuary")

(defn filter-by-ftype [df ftype]
  (r/bra df
         (r/r== ($ df 'FTYPE_DESC) ftype)
         nil))

;; Draw basemap
(-> (ggplot)
    (r/r+ #_(state-basemap "new jersey")
          (geom_polygon :data (ggplot2/map_data "state" :region "new jersey")
                        (aes :x 'long :y 'lat :group 'group)
                        :fill "#F7F5F088" :color "#333333" :size 0.2)
          (theme_void)
          (ggplot2/coord_map)))

;; Draw lakes
(-> (ggplot)
    (r/r+ (state-basemap "new jersey")
          ; draw by "adding layers" to the plot
          (geom_sf :data (filter-by-ftype new-jersey-water "Lake/Pond")
                   (aes :geometry 'geometry)
                   :fill "#72bfcf" :size 0.1)
          (coord_sf)
          (theme_void)))
;; Notice the basemap isn't showing up

;; Let's look at the projections
(sf/st_crs new-jersey-water)

(sf/st_crs colorado-water)
;;    CRS arguments: +proj=longlat +ellps=WGS84 +no_defs

;; transform new-jersey-water to use the same crs as the
;; colorado lakes shapefile
(def nj-water-transformed
  (sf/st_transform new-jersey-water (sf/st_crs colorado-water)))

;; Fix issue
(-> (ggplot)
    (r/r+ (state-basemap "new jersey")
                                        ; draw by "adding layers" to the plot
          (geom_sf :data (filter-by-ftype nj-water-transformed "Lake/Pond")
                   (aes :geometry 'geometry)
                   :fill "#72bfcf" :size 0.05)
          (coord_sf)
          (theme_void)))

;; Let's plot all this water and see the output in a pdf
(def water-plot
  (-> (ggplot)
      (r/r+ (state-basemap "new jersey")
                                        ; draw by "adding layers" to the plot
            (geom_sf :data (filter-by-ftype nj-water-transformed "Lake/Pond")
                     (aes :geometry 'geometry)
                     ;:color "NA"
                     :fill "#72bfcf" :size 0.05)

            (geom_sf :data (filter-by-ftype nj-water-transformed "Canal/Ditch")
                     (aes :geometry 'geometry)
                     ;:color "NA"
                     :fill "#72bfcf" :size 0.05)

            (geom_sf :data (filter-by-ftype nj-water-transformed "Stream/River")
                     (aes :geometry 'geometry)
                     ;:color "NA"
                     :fill "#72bfcf" :size 0.05)
            (coord_sf)
            (theme_void))))
water-plot

(def output-location (str (System/getProperty "user.dir") "/resources/output"))
(ggplot2/ggsave (str output-location "/nj_water.pdf") :plot water-plot :device "pdf")

;; Examine pdf


;; Let's look at lakes again

;; Can we simplify the shapes, since we lose a lot of detail anyway?
(def nj-water-simplified (rmapshaper/ms_simplify nj-water-transformed :keep 0.1))
;; THIS WILL TAKE A WHILE

(def simplified-plot
  (-> (ggplot)
      (r/r+ (state-basemap "new jersey")
                                        ; draw by "adding layers" to the plot
            (geom_sf :data (filter-by-ftype nj-water-simplified "Lake/Pond")
                     (aes :geometry 'geometry)
                                        :color "NA"
                     :fill "#72bfcf" :size 0.05)
            (+ 1 1)

            (geom_sf :data (filter-by-ftype nj-water-simplified "Canal/Ditch")
                     (aes :geometry 'geometry)
                                        :color "NA"
                     :fill "#72bfcf" :size 0.05)

            (geom_sf :data (filter-by-ftype nj-water-simplified "Stream/River")
                     (aes :geometry 'geometry)
                                        :color "NA"
                     :fill "#72bfcf" :size 0.05)
            (coord_sf)
            (theme_void))))

(ggplot2/ggsave (str output-location "/nj_water_simplified.pdf") :plot simplified-plot :device "pdf")

;; This is where things start getting... weird?

;; Smoothing out lines
;; https://github.com/ateucher/rmapshaper

;; Look at original vintage map
;; Can we lay these out this into a grid?

;; With the magic of R, we can!
;; TODO: show image of small multiples
;; We need to get the center of each lake

;;

(r/r->clj (base/colnames new-jersey-water))

(def order-by-shape-area (r/r "function(df) { df[order(-df$SHAPE_Area),] }"))
;; I used raw R code because it took me a while to figure out how
;; to get `order` to work. I should have asked for help sorry!

(def nj-lakes-ordered
  (-> (filter-by-ftype nj-water-transformed "Lake/Pond")
      ;; nj_water_ordered <- nj_water[order(-nj_water$shape_area),]
      order-by-shape-area
      (r/bra (r/colon 0 10) nil)))

($ nj-lakes-ordered 'SHAPE_Area)


;; Original code
;;lakes$centroid <- 
;;sf::st_centroid(test_lakes$geometry) %>% 
;;sf::st_geometry()
;;
;;padding <- 0.2
;;par(mar = c(0, 0, 0, 0))
;;lake_grid_fn <- function(x) {
;;                             ggplot(test_lakes[x,]) +
;;                             geom_sf(size=0.1, color="#1D150f", fill="#c1d7d3y") +
;;                             ggtitle(test_lakes$name[x]) +
;;                             theme_void() +
;;                             theme(plot.title = element_text(size=5, hjust=0.5)) +
;;                             coord_sf(
;;                                      xlim = c(test_lakes$centroid[[x]][1]-0.13, 
;;                                                                  test_lakes$centroid[[x]][1]+0.13), 
;;                                      ylim = c(test_lakes$centroid[[x]][2]-0.08, 
;;                                                                  test_lakes$centroid[[x]][2]+0.08), 
;;                                      expand = FALSE
;;                                      )
;;                             }
;;
;;plot_list <- lapply(X=1:nrow(test_lakes), FUN=lake_grid_fn)
;;g <- cowplot::plot_grid(plotlist = plot_list, ncol=4)
;;g

(require-r '[cowplot :as cowplot])

;; find centroids
($ nj-lakes-ordered 'centroid)
(def centroids
  (-> nj-lakes-ordered
      (sf/st_centroid)
      (sf/st_geometry)))
(r/r->clj centroids)

;; save this to a new dataframe
(def nj-lakes-centroids
  (base/data-frame :geometry ($ nj-lakes-ordered 'geometry)
                   :name ($ nj-lakes-ordered 'GNIS_NAME)

                   ;; BUG: can't save this output to the centroid col
                   :centroid (-> nj-lakes-ordered
                                 (sf/st_centroid)
                                 (sf/st_geometry))))

;; BUG: this returns null
($ nj-lakes-centroids 'centroid)

(def lake_grid_fn (r/r "function(df, centroids) {
function(x) {
ggplot(df[x,]) +
ggtitle(df$GNIS_NAME[x]) +

coord_sf(
xlim=c(centroids[[x]][1]-0.13, centroids[[x]][1]+0.13),
ylim=c(centroids[[x]][2]-0.08, centroids[[x]][2]+0.08),
expand=FALSE
)

 }
}
"))

(def plot-list (base/lapply
                :X (r/colon 1 10)
                :FUN (lake_grid_fn nj-lakes-ordered centroids)))

(cowplot/plot_grid :plotlist plot-list :ncol 4)
