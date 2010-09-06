---
layout: post
title: Test case management software
---

- Possibly a thing to handle groups of linked documents, with syntactic sugar on top to specialise for given subtasks
    - Common specialisations: smarter context dependant merging - (ie of issues, blog posts, test sets/results, slides)
    - Attatchments to convert into 'active' form (ie -&gt;Gollum, -&gt;gh-issues/Ticgit -&gt;'something yet to be invented for testing')
    - Syntactic sugar for eg checking out a task, amending/publishing a blog post
        - Which all amounts to git mv unpublished/a \_posts/${SLUG}-a &amp;&amp; git commit \_posts/${SLUG}-a -m "Publish ${a}"

- Mini documents should be markup + a yaml header. The header can either be appended to the file, or can be a file with the same base name and a '.yaml' extenstion

- Some amount of arbitrary structure; for Gollum, it is *\_unpublished* and *\_posts*, for gh-issues it is 

- Commmand line
- Git backed
  That is, stores tests in some kind of flat text files in one branch, and results in another, maybe?
- Simple; plain text snippets, everything stored as flat files, using Gollum for HTML or Shoes for application or equivalent to turn into a gui
- Ability to have chains of single files, and to be able to store segments of chains in a single file, and convert to and from
  easily (ie to join a and b into one chain:
      echo -e "\n%\n" | cat a - b
  )
- Ability to have sequence and containment concatenation (ie difference between array and linked list)

- Maybe JS (like Disqus) to store pass fail?
