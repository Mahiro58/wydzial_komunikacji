package pl.projekt.projekt.external.dto;

import java.util.List;

public class VpicDecodeResponse {
    private List<Result> Results;

    public List<Result> getResults() { return Results; }
    public void setResults(List<Result> results) { Results = results; }

    public static class Result {
        private String VIN;
        private String Make;
        private String Model;
        private String ModelYear;

        public String getVIN() { return VIN; }
        public void setVIN(String VIN) { this.VIN = VIN; }

        public String getMake() { return Make; }
        public void setMake(String make) { Make = make; }

        public String getModel() { return Model; }
        public void setModel(String model) { Model = model; }

        public String getModelYear() { return ModelYear; }
        public void setModelYear(String modelYear) { ModelYear = modelYear; }
    }
}
