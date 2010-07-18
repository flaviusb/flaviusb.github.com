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
  about: 'http://flaviusb.net/about';
  writing
    blog: 'http://flaviusb.net/blog';
    twitter: '@flaviusb';
    other: 'http://flaviusb.net/other'.;
  code
    name: 'OpenCell' repo: 'http://cellml-opencell.hg.sourceforge.net/hgweb/cellml-opencell/cellml-opencell' description: 'A CellML Simulation and Editing environment' documentation: 'http://opencell.org' ;
    name: 'CellML API' repo: 'http://cellml-api.hg.sourceforge.net/hgweb/cellml-api/cellml-api' description: 'The official CellML API' ;
    name: 'CellML Metadata Editor' repo: 'http://github.com/flaviusb/metadata-editor' description: 'An editor for author metadata embedded in CellML files.' documentation: 'http://flaviusb.net/metadata-editor/' ;
    name: 'LL' repo: 'http://github.com/flaviusb/LL' description: 'An experiment in web programming with pg''s Arc';
    name: 'other' repo: 'http://github.com/flaviusb' description: 'The place my other code goes'.;
  larp: 'http://nexus.gen.nz';
  cv.
