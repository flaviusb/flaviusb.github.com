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
  name: 'Justin Marsh'
  about: 'flaviusb.net/about';
  writing
    blog: 'flaviusb.net/blog';
    twitter: 'twitter.com/flaviusb';
    other: 'flaviusb.net/other'.;
  code
    name: 'OpenCell' repo: description: documentation: ;
    name: 'CellML API' repo: description: ;
    name: 'CellML Metadata Editor' repo: 'github.com/flaviusb/metadata-editor/' description: 'An experiment ';
    name: 'LL' repo: 'github.com/flaviusb/LL' description: 'An experiment in web programming with pg''s Arc';
    name: 'other' repo: 'github.com/flaviusb' description: 'The place my other code goes'.;
  cv.
