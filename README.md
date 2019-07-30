# innovation-competition

# Nosco Hiring Challenge

There is an *innovation competition* across the land, and the various
houses compete. All the ideas have been collected and scored, and now is the time
to calculate the results to find out the most innovative houses.

- The `innovation score` of a house is simply the `average scores` of all the ideas
submitted by people affiliated with this house. 

- Higher is better. 

- Ideas with no scores are excluded from consideration.

- Some people are affiliated with no house, in which case they should be counted as if in the house 'Free folk'.

- Some people are affiliated with more than one house, in which case the idea is credited to each and every one of the affiliated houses. For example, if user A is affiliated with houses X and Y, and they have submitted an idea that has a score of 5.7, both house X and Y will add a score of 5.7 in their tally.

## Input Data

The input data is two JSON files, and their EDN counterparts (containing the same data).

- `users.json` contains various users, each having an id first name, last name, email, and potentially a list of their house affiliation(s).

- `ideas.json` contains various ideas, each one having an id, a title, a body, an author-id (pointing to one entry in the `users.json` file) and an array of numeric scores. Unfortunately some scores were lost and were replaced by nulls. These scores should be ignored entirely.


## Expected output

The results should include:

[ ] an list of the houses, from most innovative to least innovative
[ ] the innovation score of each house
[ ] the number of ideas submitted by each house

## Other considerations

[ ] Reasonable performance is expected, but readability of the code is more
important. Use descriptive names and add docstrings and comments as needed.

[ ] You're free to use any 3rd-party library that seems suitable for the
task, keeping in mind how it might affecte the readability of the code for someone
who isn't familiar with it.

[ ] The code should include instructions on how to run it and get the results.

[ ] Any reasonably popular build tool is fine, or a single file with
side-effectful statements will also do. Use the tool that will allow
you to move to actually solving the problem.

[ ] There's no need to write a test suite for this exercise.






A Clojure library designed to ... well, that part is up to you.

## Usage

FIXME

## License

Copyright © 2019 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
