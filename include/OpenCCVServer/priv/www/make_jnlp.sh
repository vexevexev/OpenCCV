head=`cat ./jnlp_head`
tail=`cat ./jnlp_tail`
jnlp=$head$1$tail
echo -e "$jnlp" > ./OpenCCV.jnlp
