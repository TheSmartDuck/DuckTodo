package top.smartduck.ducktodo.model.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphSummaryResponse {
    private int nodeCount;
    private int edgeCount;
    private List<NodeTypeBucket> nodeTypes;
    private List<EdgeTypeBucket> edgeTypes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeTypeBucket {
        private String type;
        private int count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EdgeTypeBucket {
        private String type;
        private int count;
    }
}

