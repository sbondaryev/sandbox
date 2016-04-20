(ns worker.worker
  (:require [goog.dom :as dom] [cljs.reader]))

(declare cljs-output-file)

(def *closure-base-path* goog/basePath)
(def *closure-base-file* (str *closure-base-path* "base.js"))
(def *serialize* pr-str)
(def *deserialize* cljs.reader/read-string)
(def *env-objs* {
  :document {:getElementsByTagName "function() {return [];}"}
})

(defn scripts-src []
  (let [scripts (.getElementsByTagName (dom/getDocument) "SCRIPT")]
    (->> (for [i (range (.-length scripts))] (aget scripts i))
         (remove #(empty? (.-src %)))
         (map #(.-src %)))))

(defn cljs-output-file []
  (if-not (empty? *closure-base-path*)
    (str *closure-base-path* "../cljs_deps.js")
    (first (scripts-src))))

(defn generate-obj-body [obj]
  (->> obj
    (map (fn [[key val]] (str (name key) ":" val)))
    (interpose ",")
    (#(str "{" (apply str %) "}"))))

(defn genetate-env [objs]
  (reduce
    (fn [res [obj body]] (str res "var " (name obj) "=" (generate-obj-body body) ";"))
    "" objs))

(def *cljs-output-file* (cljs-output-file))
(def *env-str* (genetate-env *env-objs*))

(defn ^:export pr-str-js [code] (*serialize* code))

(defn create-worker-body []
  (let [
    multi-loader (str
     "var CLOSURE_BASE_PATH = '" *closure-base-path* "';"
     "var CLOSURE_IMPORT_SCRIPT = (function(global) {"
     "return function(src) {"
        ;;"global['console'].log(src);"
        "global['importScripts'](src);"
        "return true;"
     "};"
     "})(self);"
     "importScripts('" *closure-base-file* "','" *cljs-output-file* "');"
     "goog.require('" ns* "');"
     "goog.require('worker.worker');")
    single-loader (str
      "importScripts('" *cljs-output-file* "');"
    )]
  (str
    "var document=" (serialize *document*) ";"
    (if (empty? *closure-base-path*) single-loader multi-loader)
    "self.onmessage = function(e) {"
      "var res = " ns* "." fn* ".apply();"
      "self.postMessage(worker.worker.pr_str_js(res));"
    "};")))

(defn full-func-name [wrk]
  (js->clj (.split (str wrk) "/")))

(defn do-some [wrk]
  (let [a (worker-body (full-func-name wrk))
        b (js/Blob. (clj->js [a]))
        w (js/Worker. (.createObjectURL js/URL b))]
        (println "add event")
    (println a)
    (set! (.-onmessage w) (fn [e] (println (:prnt (cljs.reader/read-string (.-data e))))))
    (.postMessage w nil)))
