fs   = require 'fs'
path = require 'path'
jade = require 'jade'

process.env.TZ = 'Pacific/Auckland'

str2fancytext = (str) ->
  str = str.replace(/&quot;https?:\/\/[^&]*&quot;/g, (link) ->
    "&quot;<a href=\"#{link[6...-6]}\">#{link[6...-6]}</a>&quot;")
  str = str.replace(/&quot;@[^&]*&quot;/g, (screenname) ->
    "&quot;<a href=\"http://twitter.com/#{screenname[7...-6]}\">#{screenname[6...-6]}</a>&quot;")
  str

writeIndex =  (error, jadeat) ->
  if error?
    throw error
  console.log "Writing index.html"
  fs.writeFile (__dirname + "/index.html"), jadeat
  console.log "Done with index"

fs.readFile 'ioke.html', 'utf-8', (err, data) ->
  if err?
    console.log err
  jade.renderFile (__dirname + "/index.jade"), { locals: { code: str2fancytext(data) } }, writeIndex
