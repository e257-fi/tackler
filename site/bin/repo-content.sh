# vim: tabstop=4 shiftwidth=4 softtabstop=4 smarttab expandtab autoindent

mkdir -p repo/tests

echo "<html><body></body></html>" > repo/index.html
echo "<html><body></body></html>" > repo/tests/index.html

grep \
    '{repolink}/' \
    _docs/licenses.adoc \
    _docs/auditing.adoc \
    _docs/examples/trimix-filling-station.adoc \
    _docs/json.adoc \
    _docs/report-*.adoc \
    _docs/export-*.adoc \
    | \
    sed -E 's@.*\{repolink\}/(.*)\[.*@\1@' | \
    while read f;
    do
        fsname=$(dirname $f)/$(basename $f)
        trgdir=repo/$(dirname $f)
        src=$(basename $f)
        trg=$trgdir/$src
        trg_adoc=$trg.adoc
        
        
        echo "adoc: $fsname"
        
        #echo $trgdir
        #echo $src
        #echo $trg
        #echo $trg_adoc

        mkdir -p $trgdir
        echo "<html><body></body></html>" > $trgdir/index.html

        echo "= $src" > $trg_adoc
        echo ":page-permalink: /$trg/" >> $trg_adoc
        echo ":page-robots: noindex" >> $trg_adoc
        echo "" >> $trg_adoc
        echo "In repository: {gitlink}/$fsname[$fsname]" >> $trg_adoc
        echo '....' >> $trg_adoc
        cat ../$fsname >> $trg_adoc
        echo '....' >> $trg_adoc

    done

