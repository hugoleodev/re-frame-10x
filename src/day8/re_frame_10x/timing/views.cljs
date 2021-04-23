(ns day8.re-frame-10x.timing.views
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :refer [px]]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core :refer [defclass]]
    [day8.re-frame-10x.utils.re-com :as rc]
    [day8.re-frame-10x.styles :as styles]
    [day8.re-frame-10x.settings.subs :as settings.subs]
    [day8.re-frame-10x.timing.subs :as timing.subs]
    [day8.re-frame-10x.view.components :as components]))

(defn ms->str
  [ms]
  (cond
    (nil? ms) "-"
    (= ms 0) (str "0ms")
    (< ms 0.1) (str "<0.1ms")
    (< ms 1) (str (.toFixed ms 1) "ms")
    (some? ms) (str (js/Math.round ms) "ms")))

(defclass tag-style
  [ambiance]
  {:composes (styles/frame-2 ambiance)})

(defn tag
  [{:keys [label time]}]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/v-box
     :align    :center
     :gap      styles/gs-5s
     :children
     [[rc/label :label label]
      [components/tag (tag-style ambiance) (ms->str time)]]]))

(defclass section-style
  [ambiance]
  {:composes      (styles/frame-1 ambiance)
   :padding       styles/gs-12
   :margin-bottom styles/gs-19})

(defn elapsed
  []
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])
        time     @(rf/subscribe [::timing.subs/total-epoch-time])]
    [rc/h-box
     :class    (section-style ambiance)
     :align    :center
     :gap      styles/gs-12s
     :children
     [[rc/box
       :align   :center
       :justify :center
       :width   styles/gs-81s
       :child
       [rc/label
        :label "elapsed"]]
      [tag
       {:label ""
        :time  time}]
      [rc/hyperlink
       :class  (styles/hyperlink ambiance)
       :label  "guide me to greatness"
       :target "_blank"
       :href   "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/UnderstandingTiming.md"]]]))

(defn event-processing
  []
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])
        times    @(rf/subscribe [::timing.subs/event-processing-time])]
    [rc/h-box
     :class    (section-style ambiance)
     :align    :center
     :gap      styles/gs-12s
     :children
     [[rc/v-box
       :align    :center
       :width    styles/gs-81s
       :children
       [[rc/label
         :label "event"]
        [rc/label
         :label "processing"]]]
      [tag
       {:label "total"
        :time  (:timing/event-total times)}]
      [:span "="]
      [tag
       {:label "handler"
        :time  (:timing/event-handler times)}]
      [:span "+"]
      [tag
       {:label "effects"
        :time  (:timing/event-effects times)}]
      [:span "+"]
      [tag
       {:label "misc"
        :time  (:timing/event-misc times)}]]]))

(defn animation-frames
  []
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])
        n        @(rf/subscribe [::timing.subs/animation-frame-count])]
    (into
      [:<>]
      (for [i    (range 1 (inc n))
            :let [times @(rf/subscribe [::timing.subs/animation-frame-time i])]]
        [rc/h-box
         :class (section-style ambiance)
         :align :center
         :gap   styles/gs-12s
         :children
         [[rc/v-box
           :align    :center
           :width    styles/gs-81s
           :children
           [[rc/label
             :label "animation"]
            [rc/label
             :label "frame"]
            [rc/label
             :label (str "#" i)]]]
          [tag
           {:label "total"
            :time  (:timing/animation-frame-total times)}]
          [:span "="]
          [tag
           {:label "subs"
            :time  (:timing/animation-frame-subs times)}]
          [:span "+"]
          [tag
           {:label "views"
            :time  (:timing/animation-frame-render times)}]
          [:span "+"]
          [tag
           {:label "react, etc"
            :time  (:timing/animation-frame-misc times)}]]]))))

(defn panel
  []
  (let [available? @(rf/subscribe [::timing.subs/data-available?])]
    (if available?
      [rc/v-box
       :children
       [[elapsed]
        [event-processing]
        [animation-frames]]]
      [rc/v-box
       :children
       [[rc/label
         :label "No timing data is currently available."]]])))