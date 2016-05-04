(ns songcontest.pages
    (:require
      [hiccup.core :refer [html]]
      [hiccup.page :refer [include-css include-js]]))

(defn index 
  []
  (html 
     [:head
       [:title "Songcontest - Index"]
       (include-css "css/style.css")
       (include-css "webjars/bootstrap/3.2.0/css/bootstrap.min.css")
       (include-css "webjars/bootstrap/3.2.0/css/bootstrap-theme.min.css")]
     [:body
       [:p
         [:a {:href "contest"} "Contest"]
         [:a {:href "nomination"} "Nomination"]
         [:a {:href "rating"} "Rating"]]]))

(defn user  
  [id]
  (html
     [:head
       [:title (str "Songcontest - " (name id))]
       (include-css "css/style.css")
       (include-css "webjars/bootstrap/3.2.0/css/bootstrap.min.css")
       (include-css "webjars/bootstrap/3.2.0/css/bootstrap-theme.min.css")]
     [:body
       [:p
         [:a {:href "contest"} "Contest"]
         [:a {:href "nomination"} "Nomination"]
         [:a {:href "rating"} "Rating"]]

       [:div {:id "app"}] 
      
       (include-js "webjars/jquery/1.11.1/jquery.min.js")
       (include-js "webjars/bootstrap/3.2.0/js/bootstrap.min.js")
       (include-js "out/goog/base.js")
       (include-js "app.js")
       [:script  
         {:type "text/javascript"} 
         (format "goog.require(\"songcontest.%s\");
                  songcontest.%s.render_component();" (name id) (name id))]]))