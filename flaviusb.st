Object subclass: AnaphoricCollectionBuilder [
  | elements |
  <category: 'Showing Off'>
  printOn: aStream [
    <category: 'printing'>
    aStream
  ]
]
| flaviusb |
":flaviusb is a handle of Justin Marsh"
flaviusb =: AnaphoricCollectionBuilder new
  name: 'Justin Marsh';
  about: 'flaviusb.net/about';
  writing
    blog: 'flaviusb.net/blog';
    twitter: 'twitter.com/flaviusb';
    other: 'flaviusb.net/other'.;
  code
    name: 'OpenCell' repo: 'http://cellml-opencell.hg.sourceforge.net/hgweb/cellml-opencell/cellml-opencell' description: 'A CellML Simulation and Editing environment' documentation: 'opencell.org' ;
    name: 'CellML API' repo: 'http://cellml-api.hg.sourceforge.net/hgweb/cellml-api/cellml-api' description: 'The official CellML API' ;
    name: 'CellML Metadata Editor' repo: 'github.com/flaviusb/metadata-editor/' description: 'An editor for author metadata embedded in CellML files.';
    name: 'LL' repo: 'github.com/flaviusb/LL' description: 'An experiment in web programming with pg''s Arc';
    name: 'other' repo: 'github.com/flaviusb' description: 'The place my other code goes'.;
  larp: 'nexus.gen.nz';
  cv.
