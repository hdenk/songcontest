(ns songcontest.page
    (:require
      [hiccup.core :refer [html]]
      [hiccup.page :refer [include-css include-js]]))

(defn to-argumentlist [params]
  (if params
    (clojure.string/join ", " (map (fn [x] (if (string? x) 
                                             (str \" x \")
                                              x)) 
                                   params))
    ""))

(defn index 
  []
  (html 
     [:head
       [:title "Songcontest - Index"]
       (include-css "/css/style.css")
       (include-css "/webjars/bootstrap/3.2.0/css/bootstrap.min.css")
       (include-css "/webjars/bootstrap/3.2.0/css/bootstrap-theme.min.css")]
     [:body
       [:p
         [:a {:href "contest"} "Contest"]
         [:a {:href "song"} "Song"]]]))

(defn default  
  [name & params]
  (html
     [:head
       [:title (str "Songcontest")]
       (include-css "/css/style.css")
       (include-css "/webjars/bootstrap/3.2.0/css/bootstrap.min.css")
       (include-css "/webjars/bootstrap/3.2.0/css/bootstrap-theme.min.css")]
     [:body
       [:p
         [:a {:href "contest"} "Contest"]
         [:a {:href "song"} "Song"]]

       [:div {:id "app"}] 
      
       (include-js "/webjars/jquery/1.11.1/jquery.min.js")
       (include-js "/webjars/bootstrap/3.2.0/js/bootstrap.min.js")
       (include-js "/out/goog/base.js")
       (include-js "/app.js")
       [:script  
         {:type "text/javascript"} 
         (format "goog.require(\"songcontest.%s\");
                  songcontest.%s.render_component(%s);" name name (to-argumentlist params))]]))


(to-argumentlist '(13 "hans" 14 "sepp"))