
ymd=$1
page=$2

head -n1 $page > $page.tmp
echo ":page-date: $ymd 00:00:00 Z" >> $page.tmp
echo ":page-last_modified_at: $ymd 00:00:00 Z" >> $page.tmp
tail -n+4 $page >> $page.tmp

mv $page.tmp $page

