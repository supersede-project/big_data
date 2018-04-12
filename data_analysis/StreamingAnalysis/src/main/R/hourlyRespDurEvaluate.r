args = commandArgs(trailingOnly=TRUE)
t.start =Sys.time()
## test if there is at least one argument: if not, return an error

#args=("C:\\Users\\Z003TF1W\\Google Drive\\Siemens\\Code\\SVN\\part-eval")

if (length(args)==0) {
  stop("At least one argument must be supplied (input file)", call.=FALSE)
}
if (length(args)==1) {
  # default input file
  args[2] = "thrsh_out.csv"
}
if (length(args)==2) {
    # default output file
    args[3] = "alarm.csv"
}
if (length(args)==3) {
      # default input file
      args[4] = "MethodClustering.csv"}

######################################
##initial setup

# Function to check whether package is installed
install.req.if.not <- function(mypkg){
  if (!is.element(mypkg, installed.packages()[,1])){
    install.packages(mypkg)}else{library(mypkg,character.only = TRUE)}}

#misc
options(repos = "http://cran.rediris.es/")
options(digits.secs = 3)
Sys.setenv(LANGUAGE="en")
Sys.setlocale("LC_ALL", "English")

#required packages
packages=c(
  "data.table",
  "R.filesets",
  "RecordLinkage")

#check if required packages are installed, install if not, else- require 
sapply(packages,FUN=install.req.if.not)

######################################
## program...
#dat = read.table(args[1], header=TRUE)

#fread instead
coln=c("LogType","Timestamp.old","Class","User","Role","SessionID",
       "EventType","Direction","MethodNameOrig","ContentType","Content","TransID")

keep=c(1,2,6,8,9)
my.data <- fread(args[1], col.names = coln[keep],
                 select=keep,
                 header=FALSE,fill=TRUE,sep="|",blank.lines.skip	=T,dec=",",quote='')

my.data=my.data[LogType=="TRACE",.(Timestamp.old,MethodNameOrig,Direction,SessionID)]
my.data=my.data[Direction!=""]

my.data[,Timestamp:=as.POSIXct(strptime(Timestamp.old,  "%Y-%m-%d %H:%M:%OS"))]
my.data[is.na(Timestamp),Timestamp:=as.POSIXct(strptime(Timestamp.old,  "%Y-%m-%dT%H:%M:%OS%z"))]

my.data[,c("MethodNameOnly","MethodParams"):=data.table(t(sapply(lapply(
  as.character(my.data$MethodNameOrig),function(x){
    y=strsplit(x,"?",fixed=T)[[1]]
    if(length(y)==0){return(c("",""))}
    if(length(y)==1){return(c(y[1],""))}
    else{return(y[1:2])}}), "[")))]

my.data[,MethodParams:=NULL]

#delete the prefixes
to.del=c("http://localhost:6080","http://localhost","http://demo_ecosys:6080/ecosyscore/apiconsume/call",
         "http://10.50.1.100:6080","http://10.50.1.100","call","http://127.0.0.1:6080")

for (i in 1:length(to.del)){
  ind.here=grep(to.del[i],my.data$MethodNameOnly,value=F,fixed=T)
  if (length(ind.here)==0) next
  my.data[ind.here,MethodNameOnly:=gsub(to.del[i],"",MethodNameOnly,fixed=T)]}

my.data[,Timestamp:=as.POSIXct(strptime(Timestamp.old,  "%Y-%m-%d %H:%M:%OS"))]
meth.n.cl=fread(args[4],sep=";")

indic=match(my.data$MethodNameOnly,meth.n.cl$MethodNameOnly)

my.data[,MethodNameOnlyGrouped := meth.n.cl[indic,"ClusterName"]]
my.data[,API := meth.n.cl[indic,"API"]]

#in case of new unseen method names
######################################
if(nrow(my.data[is.na(MethodNameOnlyGrouped)])>0){
  Newclust=apply(my.data[is.na(MethodNameOnlyGrouped),.(unique(MethodNameOnly))],1,function(x){levenshteinSim(x,as.character(meth.n.cl$MethodNameOnly))})
  ncl.maxind=apply(Newclust,2,which.max)
  
  ncl.max=numeric(length(ncl.maxind))
  for(i in 1:length(ncl.maxind)){
    ncl.max[i]=Newclust[ncl.maxind[i],i]}
  
  toch=is.na(my.data[,MethodNameOnlyGrouped])
  indic=match(my.data[is.na(MethodNameOnlyGrouped),MethodNameOnly],my.data[is.na(MethodNameOnlyGrouped),unique(MethodNameOnly)])
  
  my.data[toch,MethodNameOnlyGrouped := meth.n.cl[ncl.maxind,"ClusterName"][indic]]
  my.data[toch, API:= meth.n.cl[ncl.maxind,"API"][indic]]}

dur=my.data[,.(Time=min(Timestamp),RESPONSE=max(Timestamp),REQUEST=min(Timestamp),Method=head(MethodNameOnlyGrouped,1),API=head(API,1),.N),by="SessionID"]

dur[,Duration:=RESPONSE-REQUEST]
dur=dur[Duration>0 
        & Duration<5000
        & Duration!=Inf,]

df_out=dur[,mean(Duration),by="Method"]
colnames(df_out)=c("GroupedMethodName","ResponseDuration")

thrs=fread(args[2],sep=",")
setkey(thrs,GroupedMethodName)
setkey(df_out,GroupedMethodName)

getwd()
write.csv(df_out[thrs][ResponseDuration>DurationThresh], file=args[3], row.names=FALSE)
paste("Execution duration:",round(Sys.time()-t.start,2),'seconds.')
