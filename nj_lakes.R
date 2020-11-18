dloc =  "/home/joannecheng/dev/viz/clojure-map-examples/resources/data_files/new_jersey/NHDWaterbody_2002_shapefile"
lakes_sf = paste(dloc,
                 "/NHDWaterbody_2002.shp",
                 sep="")
nj_water <- sf::st_read(lakes_sf)

colnames(nj_lakes)

library(ggplot2)
library(maps)
library(sf)
library(raster)
library(rgdal)
library(rmapshaper)

nj <-  map_data("state", region="new jersey")
raster::crs(nj_water)
njcrs <-  raster::crs("+proj=tmerc +lat_0=38.8333333333333 +lon_0=-74.5 +k=0.9999 +x_0=150000 +y_0=0 +datum=NAD83 +units=us-ft +no_defs")
sp::spTransform(nj_lakes, njcrs)

ggplot() +
  geom_polygon(data= nj, aes(x=long, y=lat), fill="#ffffff", color="#333333") +
  geom_sf(data=nj_water[1:200,], aes(geometry=geometry)) +
  coord_sf()
  
raster::crs(map_data("state", region="new jersey"))

smoothr::smooth(nj_water, method="ksmooth")

library(smoothr)
nj_water

nj_simplified<-rmapshaper::ms_simplify(nj_water, keep=0.001)

ggplot() +
  geom_polygon(data= nj, aes(x=long, y=lat), fill="#ffffff", color="#333333") +
  geom_sf(data=nj_simplified[1:1000,], aes(geometry=geometry)) +
  coord_sf()

nj_ordered <- nj_simplified[order(-nj_simplified$SHAPE_Area),]
