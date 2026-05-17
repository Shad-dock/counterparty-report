package org.example.counterparty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class DaDataResponse {

    private List<Suggestion> suggestions;

    public List<Suggestion> getSuggestions() { return suggestions; }
    public void setSuggestions(List<Suggestion> suggestions) { this.suggestions = suggestions; }

    public Suggestion getFirstSuggestion() {
        if (suggestions != null && !suggestions.isEmpty()) {
            return suggestions.get(0);
        }
        return null;
    }

    public static class Suggestion {
        private String value;
        private String unrestrictedValue;
        private Data data;

        @JsonProperty("unrestricted_value")
        public String getUnrestrictedValue() { return unrestrictedValue; }
        public void setUnrestrictedValue(String unrestrictedValue) { this.unrestrictedValue = unrestrictedValue; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }

        public Data getData() { return data; }
        public void setData(Data data) { this.data = data; }
    }

    public static class Data {
        private String inn;
        private String ogrn;
        private Name name;
        private Address address;
        private State state;

        @JsonProperty("registration_date")
        private String registrationDate;

        public String getInn() { return inn; }
        public void setInn(String inn) { this.inn = inn; }

        public String getOgrn() { return ogrn; }
        public void setOgrn(String ogrn) { this.ogrn = ogrn; }

        public Name getName() { return name; }
        public void setName(Name name) { this.name = name; }

        public String getFullName() {
            return name != null ? name.getFull() : null;
        }

        public Address getAddress() { return address; }
        public void setAddress(Address address) { this.address = address; }

        public State getState() { return state; }
        public void setState(State state) { this.state = state; }

        public String getRegistrationDate() { return registrationDate; }
        public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
    }

    public static class Name {
        private String full;

        public Name() {}
        public Name(String full) { this.full = full; }

        public String getFull() { return full; }
        public void setFull(String full) { this.full = full; }
    }

    public static class Address {
        private String value;

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public static class State {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}