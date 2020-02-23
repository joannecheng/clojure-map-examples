library(maps)
library(geosphere)

getGreatCircle <- function(userLL,relationLL){
  tmpCircle = greatCircle(userLL,relationLL, n=200)
  start = which.min(abs(tmpCircle[,1] - data.frame(userLL)[1,1]))
  end = which.min(abs(tmpCircle[,1] - relationLL[1]))
  greatC = tmpCircle[start:end,]
  return(greatC)
}

# No margin
par(mar=c(0,0,0,0))

denver <- c(-104.9903, 39.7392) 
berlin <- c(13.4050, 52.5200)

great <- getGreatCircle(denver, berlin)

# World map
map('world',
    col="#d2d2d2", fill=TRUE, bg="white", lwd=0.05,
    mar=rep(0,4),border=0, ylim=c(-20,80), xlim=c(-140, 40))
lines(great, col="#c05137", lwd=3)
