# FSP tool(s)

## Generate mappings

	cd testsuite
	mvn javadoc:test-javadoc -PccTsfi -DallTests

## Generate mappings

	git grep @test.tsfi | sed "s#.*test.tsfi \(.*\)#\1#"|sort |uniq

## Get labels with SFRs from fsp.xml

	xmllint --xpath "//tsfi[sfrs/sfr]/label[string-length() > 0]/text()" fsp/fsp.xml | sed "s#tsfi\.#\ntsfi.#g"|sort |uniq

## Create xinclude lines in tsfi-mappings-xinclude.xml

	find . -name tsfi-mapping.xml| while read i; do echo '<xi:include href="'$i'" parse="xml" xpointer="xpointer(/testCaseMapping/testSuite/test)"/>'; done;
	
## Generate the merged file

	xmllint --xinclude tsfi-mappings-xinclude.xml |grep -v 'xi:include' > ~/tmp/tsfi-mappings-$(date "+%y%m%d").xml 
