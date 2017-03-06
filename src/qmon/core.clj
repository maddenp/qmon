(ns qmon.core
  (:gen-class)
  (:import [java.awt BorderLayout Color Font]
           [java.awt.event MouseAdapter]
           [javax.swing JButton JFrame JPanel JScrollPane JTextArea])
  (:use [clojure.java.shell :only [sh]]
        [clojure.string :only [blank?, join]]
        [clj-xpath.core :only [$x $x:text]]))

(def headkeys [:jobid :owner :rtime :utime :nodes :state :queue :name])
(def headvals ["Job ID" "Owner" "Requested" "Used" "Nodes" "S" "Queue" "Job Name"])
(def waitmsg "\nLoading...")

(declare split)

(defn colwidths [jobs]
  (let [headwidth (map #(count ((zipmap headkeys headvals) %)) headkeys)]
    (if (empty? jobs)
      headwidth
      (let [datawidth (map #(reduce max (map count (map % jobs))) headkeys)]
        (map max headwidth datawidth)))))

(defn extract-jobs []
  (try
    (let [xml (:out (sh "qstat" "-x"))]
      (if (blank? xml)
        []
        (let [jobs ($x "//Job" xml)]
          (reduce (fn [m e]
                    (let [dig #($x:text % e)]
                      (conj m {:jobid (re-find #"^[0-9]+" (dig "."))
                               :name (dig "./Job_Name")
                               :nodes (dig "./Resource_List/nodes")
                               :owner (re-find #"^[^@]+" (dig "./Job_Owner"))
                               :queue (dig "./queue")
                               :rtime (dig "./Resource_List/walltime")
                               :state (dig "./job_state")
                               :utime (dig "./resources_used/walltime")})))
                  []
                  jobs))))
    (catch Exception e
      (binding [*out* *err*]
        (println (.getMessage e))))))

(defn myjobs [user]
  (let [jobs (extract-jobs)]
    (filter #(= user (:owner %)) jobs)))

(defn prhead [msg fmt sep]
  (str (format "\n%s\n\n" msg) (apply format fmt headvals) "\n" sep "\n"))

(defn prjobs [fmt sep jobs msg]
  (let [fmtjob (fn [j fmt] (apply format fmt (map #(% j) headkeys)))]
    (if (empty? jobs)
      nil
      (str (prhead msg fmt sep) (reduce #(str %1 %2) (map #(str (fmtjob % fmt) "\n") jobs))))))

(defn show [user]
  (let [jobs      (myjobs   user)
        sections  (split    jobs)
        colwidths (colwidths jobs)
        fmt       (join " " (map #(str "%-" % "s") colwidths))
        dashes    (fn [n] (apply str (repeat n "-")))
        sep       (join " " (map #(dashes %) colwidths))]
    (str (format "\n%s\n" (.toString (java.util.Date.)))
         (prjobs fmt sep (:Q sections) "QUEUED"   )
         (prjobs fmt sep (:R sections) "RUNNING"  )
         (prjobs fmt sep (:C sections) "COMPLETED"))))

(defn split [jobs]
  (let [f (fn [s j] (filter #(= s (:state %)) j))]
    (apply merge (map #(hash-map % (f (name %) jobs)) [:Q :R :C]))))

(defn -main [& args]
  (let [user         (or (first args) (get (System/getenv) "USER"))
        panel        (JPanel. (BorderLayout.))
        text-area    (JTextArea. waitmsg)
        button       (JButton. "Sleep")
        button-panel (JPanel.)
        scroll-pane  (JScrollPane. text-area)
        active       (atom true)
        toggle       (fn [x]
                       (.setBackground text-area (if x (Color/DARK_GRAY ) (Color/WHITE)))
                       (.setForeground text-area (if x (Color/WHITE) (Color/DARK_GRAY )))
                       (.setText button (if x "Wake" "Sleep" ))
                       (if-not x (.setText text-area waitmsg))
                       (not x))]
    (do
      (doto text-area
        (.setFont (Font. "Monospaced" (Font/PLAIN) 12))
        (.setForeground (Color/DARK_GRAY))
        (.setEditable false))
      (doto button
        (.addMouseListener (proxy [MouseAdapter] [] (mousePressed [e] (swap! active toggle)))))
      (doto button-panel
        (.add button))
      (doto panel
        (.add scroll-pane BorderLayout/CENTER)
        (.add button-panel BorderLayout/SOUTH))
      (doto (JFrame. (str "qmon " user))
        (.setSize 800 600)
        (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
        (.add panel)
        (.setVisible true))
      (while true
        (if @active
          (let [newtext (show user)]
            (if @active (.setText text-area newtext))))
        (Thread/sleep 10000)))))
