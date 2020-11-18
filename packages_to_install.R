# Must be at least version 1.8 or else you won't be able to run any examples
install.packages("Rserve", "https://www.rforge.net/")

# If you run into problems installing tidyverse on ubuntu, check out the following blog post
# https://blog.zenggyu.com/en/post/2018-01-29/installing-r-r-packages-e-g-tidyverse-and-rstudio-on-ubuntu-linux/
# "the tidyverse package requires the following non-R packages"
# libcurl4-openssl-dev, libssl-dev, libxml2-dev.
install.packages("tidyverse", dependencies=TRUE)

# This was required to use "sf"
# make sure you install libudunits2 before installing the R package
# in ubuntu: `sudo apt-get install libudunits2-dev`
install.packages("udunits2")

# Make sure you have GDAL installed!
# Mac osx: https://trac.osgeo.org/gdal/wiki/BuildingOnMac
# Ubuntu: https://mothergeo-py.readthedocs.io/en/latest/development/how-to/gdal-ubuntu-pkg.html
install.packages("sf")
install.packages("maps")
install.packages("raster")

install.packages("devtools")
devtools::install_github("tylermorganwall/rayshader")


install.packages("svglite")
install.packages("geosphere")
install.packages("av")
