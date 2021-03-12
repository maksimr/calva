(ns calva.lsp.download
  (:require ["fs" :as fs]
            ["process" :as process]
            ["path" :as path]
            ["follow-redirects" :refer [https]]
            ["extract-zip" :as extract-zip]))

(def version-file-name "clojure-lsp-version")

(def zip-file-name
  (condp = (.. process -platform)
    "darwin" "clojure-lsp-native-macos-amd64.zip"
    "linux" "clojure-lsp-native-linux-amd64.zip"
    "win32" "clojure-lsp-native-windows-amd64.zip"))

(defn get-zip-file-path [extension-path]
  (. path (join extension-path zip-file-name)))

(defn read-version-file
  [extension-path]
  (let [file-path (. path (join extension-path
                                version-file-name))]
    (.. fs (readFileSync file-path "utf8"))))

(defn unzip-file [zip-file-path extension-path]
  (js/console.log "Unzipping file")
  (extract-zip zip-file-path
               (clj->js {:dir extension-path})))

(defn download-zip-file [url-path file-path]
  (js/console.log "Downloading clojure-lsp from" url-path)
  (js/Promise.
   (fn [resolve reject]
     (.. https
         (get (clj->js {:hostname "github.com"
                        :path url-path})
              (fn [^js response]
                (js/console.log "Got response")
                (let [write-stream (.. fs (createWriteStream file-path))]
                  (.. response
                      (on "end"
                          (fn []
                            (.. write-stream close)
                            (js/console.log "Clojure-lsp zip file downloaded to" file-path)
                            (resolve)))
                      (pipe write-stream)))))))))

(defn download-clojure-lsp [extension-path version]
  (js/console.log "Downloading clojure-lsp")
  (let [current-version (read-version-file extension-path)
        url-path (str "/clojure-lsp/clojure-lsp/releases/download/"
                      version
                      "/" zip-file-name)
        zip-file-path (get-zip-file-path extension-path)]
    (js/console.log "Current version of clojure-lsp is" current-version)
    (if (not= current-version version)
      (.. (download-zip-file url-path zip-file-path)
          (then (fn []
                  (unzip-file zip-file-path extension-path))))
      (do (js/console.log "Not downloading clojure-lsp")
          (js/Promise.resolve)))))

(comment
  (extract-zip "/home/brandon/development/calva/clojure-lsp-native-linux-amd64.zip"
               (clj->js {:dir "/home/brandon/development/calva"}))
  
  (read-version-file "/home/brandon/development/calva/clojure-lsp-version")

  (.. (js/Promise. (fn [resolve reject]
                     (resolve "hello")))
      (then (fn [value]
              (js/console.log value)))))