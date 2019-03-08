var myTemplateConfig = {
  colors: ["#999" ],
  branch: {
    lineWidth: 4,
    spacingX: 30
  },
  commit: {
    spacingY: -40,
    dot: {
      size: 8,
    }
  }
};
var myTemplate = new GitGraph.Template(myTemplateConfig);

var config = {
  template: myTemplate,
  orientation: "vertical",
  mode: "compact"
};

var gitGraph = new GitGraph(config);


/************************
 * BRANCHES AND COMMITS *
 ************************/

var master = gitGraph.branch("master");
gitGraph.commit("Initial commit");

var dev = master.branch({
  name: "Topic-1",
  color: "#00C",
  commitDefaultOptions: {
    color: "#00C"
  }
});

dev.commit("dev");

master.commit("Activity on master");

var feature3 = master.branch({
  name: "Topic-1",
  color: "#FC0",
  commitDefaultOptions: {
    color: "#FC0"
  }
});

feature3.commit("Activity on topic-3");

dev.commit("dev");

feature3.commit("Activity on topic-3");

master.commit("Activity on master")

var topic4 = master.branch({
  name: "Topic-4",
  color: "#A00",
  commitDefaultOptions: {
    color: "#A00"
  }
});

master.checkout();
dev.merge();

topic4.commit("Activity on topic-4")

feature3.commit("Activity on topic-3")

master.commit("Activity on master")

