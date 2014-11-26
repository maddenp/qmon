(ns qmon.core
  (:gen-class)
  (:import [java.awt BorderLayout Color Font]
           [java.awt.event MouseAdapter]
           [java.io StringBufferInputStream]
           [javax.swing JButton JFrame JPanel JScrollPane JTextArea])
  (:use [clojure.java.shell :only [sh]])
  (:use [clojure.string :only [join]])
  (:use [clojure.xml :only [parse]]))

(def headkeys [:jobid :owner :rtime :utime :procs :nodes :state :queue :part :name])

(def headvals ["Job ID" "Owner" "Requested" "Used" "Procs" "Nodes" "S" "Queue" "Partition" "Job Name"])

(declare split tree xprocs xres xtime)

(defn colwidths [jobs]
  (let [headwidth (map #(count ((zipmap headkeys headvals) %)) headkeys)]
    (if (empty? jobs)
      headwidth
      (let [datawidth (map #(reduce max (map count (map % jobs))) headkeys)]
        (map max headwidth datawidth)))))

(defn jobinfo [job]
  (let [fns (map #(ns-resolve 'qmon.core (symbol (str "x" (name %)))) headkeys)
        raw (fn [job] (zipmap (map #(:tag %) job) (map #(:content %) job)))]
    (zipmap headkeys (map #(% (raw job)) fns))))

(defn myname []
  (if (first *command-line-args*)
    (first *command-line-args*)
    (get (System/getenv) "USER")))

(defn myjobs [user]
  (let [alljobs (map :content (filter #(= (:tag %) :Job) (tree)))
        re (fn [user] (re-pattern (str user "@.*")))
        x (filter #(re-matches (re user) (:owner %)) (map #(jobinfo %) alljobs))]
    (map #(assoc %
            :jobid (re-find (re-matcher #"^\d+" (:jobid %)))
            :owner (re-find (re-matcher #"^[^@]+" (:owner %))))
         x)))

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

(defn tree []
  (try (:content (parse (java.io.StringBufferInputStream. (:out (sh "qstat" "-x")))))
       (catch Exception e)))

(def waitmsg "\nLoading...")

(defn xattr [j,k]
  (first (k j)))

(defn xjobid [j]
  (xattr j :Job_Id))

(defn xname [j]
  (xattr j :Job_Name))

(defn xnodes [j]
  (if (= "-" (xprocs j)) (xres j :nodect) "-"))

(defn xowner [j]
  (xattr j :Job_Owner))

(defn xpart [j]
  (let [p (first (:content (first (filter #(= (:tag %) :partition) (:Resource_List j)))))]
    (if (nil? p) "-" p)))

(defn xprocs [j]
  (let [procs (xres j :procs) ] (if (nil? procs) "-" procs)))

(defn xqueue [j]
  (xattr j :queue))

(defn xres [j,k]
  (first (:content (first (filter #(= (:tag %) k) (:Resource_List j))))))

(defn xrtime [j]
  (xtime j :Resource_List))

(defn xstate [j]
  (xattr j :job_state))

(defn xtime [j,k]
  (first (:content (first (filter #(= (:tag %) :walltime) (k j))))))

(defn xutime [j]
  (let [t (xtime j :resources_used)] (if (nil? t) "-" t)))

(defn -main [& args]
  (let [user         (myname)
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
