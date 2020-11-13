install.packages("Rserve", "http://rforge.net")

install.packages("tidyverse", dependencies=TRUE)
# If you run into problems installing tidyverse on ubuntu, check out the following blog post
# https://blog.zenggyu.com/en/post/2018-01-29/installing-r-r-packages-e-g-tidyverse-and-rstudio-on-ubuntu-linux/
# "the tidyverse package requires the following non-R packages"
# libcurl4-openssl-dev, libssl-dev, libxml2-dev.

install.packages("sf")
install.packages("maps")
install.packages("raster")

install.packages("devtools")
devtools::install_github("tylermorganwall/rayshader")


install.packages("svglite")
install.packages("geosphere")
install.packages("av")
