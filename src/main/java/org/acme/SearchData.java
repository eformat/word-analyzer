package org.acme;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.math.BigDecimal;
import java.util.Objects;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchData implements Comparable {

    private String filename;
    private String url;
    private BigDecimal score;
    private String highlight;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getHighlight() {
        return highlight;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchData that = (SearchData) o;
        return Objects.equals(filename, that.filename) && Objects.equals(url, that.url) && Objects.equals(score, that.score) && Objects.equals(highlight, that.highlight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, url, score, highlight);
    }

    @Override
    public String toString() {
        return "SearchData{" +
                "filename='" + filename + '\'' +
                ", url='" + url + '\'' +
                ", score=" + score +
                ", highlight='" + highlight + '\'' +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof SearchData)) {
            return 1;
        }
        SearchData other = (SearchData) o;
        return this.score.compareTo(other.score);
    }
}
