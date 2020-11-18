dloc =  "/home/joannecheng/dev/viz/clojure-map-examples/resources/data_files/new_jersey/NHDWaterbody_2002_shapefile"
lakes_sf = paste(dloc,
                 "/NHDWaterbody_2002.shp",
                 sep="")
nj_water <- sf::st_read(lakes_sf)

colnames(nj_lakes)

library(ggplot2)
library(maps)
library(sf)


ggplot() +
  geom_polygon(data= map_data("state", region="new jersey"),
               aes(x=long, y=lat),
               fill="#ffffff", color="#333333") +
  geom_sf(data=nj_water, aes(geometry=geometry)) +
  coord_map()
  
  
