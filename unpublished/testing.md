---
layout: post
title: Test case management software
---

- Possibly a thing to handle groups of linked documents, with syntactic sugar on top to specialise for given subtasks
    - Common specialisations: smarter context dependant merging - (ie of issues, blog posts, test sets)
    - Attatchments to convert into 'active' form (ie -&gt;Gollum, -&gt;gh-issues/Ticgit -&gt;'something yet to be invented for testing')
    - Syntactic sugar for eg checking out a task, amending/publishing a blog post

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
