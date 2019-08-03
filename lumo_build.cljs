(require 'lumo.build.api)


(lumo.build.api/build "snake.cljs" {;:target :browser | node
                                    :optimizations :simple
                                    :output-to "./out/snake.js"
                                    :asset-path "out"
                                    :output-dir "out"})
