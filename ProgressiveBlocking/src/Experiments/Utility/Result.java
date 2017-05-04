package Experiments.Utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Giovanni
 */
public class Result {

    private List<String> description;
    private List<Long> dupl_e, comp_e;
    private List<Long[]> size;
    private List<Double> t_start, t_init, t_end;

    private List<List<Double>> res_dupl, res_comp;
    private List<List<Long>> res_time;
    private List<Double> res_set_dupl, res_set_comp;
    private List<Long> res_set_time;

    public long latest_time = 0;

    private Instant instant_start;
    private Instant instant_init;
    private Instant instant_comparison;

    public Result() {
        description = new LinkedList();
        dupl_e = new LinkedList();
        comp_e = new LinkedList();
        res_dupl = new LinkedList<>();
        res_comp = new LinkedList<>();
        res_time = new LinkedList<>();
        res_set_dupl = new LinkedList<>();
        res_set_comp = new LinkedList<>();
        res_set_time = new LinkedList<>();

        size = new LinkedList();

        t_start = new LinkedList();
        t_init = new LinkedList();
        t_end = new LinkedList();
    }

    public String toJson() {
        if (res_set_comp != null) {
            res_comp.add(new LinkedList<>(res_set_comp));
            res_dupl.add(new LinkedList<>(res_set_dupl));
            res_time.add(new LinkedList<>(res_set_time));
            res_set_comp = null;
            res_set_dupl = null;
            res_set_time = null;
        }
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        return gson.toJson(this);
    }


    public void add_res_set() {
        if (!res_set_comp.isEmpty()) {
            res_comp.add(new LinkedList<>(res_set_comp));
            res_dupl.add(new LinkedList<>(res_set_dupl));
            res_time.add(new LinkedList<>(res_set_time));
        }
        res_set_comp = new LinkedList();
        res_set_dupl = new LinkedList();
        res_set_time = new LinkedList();
    }

    public void add_res(double dupl, Double count) {
        res_set_dupl.add(dupl);
        res_set_comp.add(count);
        latest_time = (Instant.now().toEpochMilli() - instant_start.toEpochMilli());
        res_set_time.add(latest_time);
    }

    public void set_dupl_e(long dupl_e) {
        this.dupl_e.add(dupl_e);
    }

    public void set_comp_e(long comp_e) {
        this.comp_e.add(comp_e);
    }

    public void start() {
        add_res_set();
        instant_start = Instant.now();
        set_t_start(Instant.now().toEpochMilli());
    }

    public void init() {
        instant_init = Instant.now();
        set_t_init(instant_init.toEpochMilli() - instant_start.toEpochMilli());

        res_set_dupl.add(0.0);
        res_set_comp.add(0.0);
        res_set_time.add(instant_init.toEpochMilli() - instant_start.toEpochMilli());
    }

    public void end() {
        instant_init = Instant.now();
        set_t_end(instant_init.toEpochMilli() - instant_start.toEpochMilli());
    }

    /**
     * Set the size of the dataset. It takes 2 long (for dirty ER the seocond is 0)
     *
     * @param size
     */
    public void set_size(Long[] size) {
        this.size.add(size);
    }

    /**
     * Set the starting time of the experiment (e.g., after that the profiles are loaded)
     *
     * @param t_start
     */
    public void set_t_start(double t_start) {
        this.t_start.add(t_start);
    }

    /**
     * Set the starting time of the progressive method (i.e., when the first next() returns a pairs of profiles)
     *
     * @param t_init
     */
    public void set_t_init(double t_init) {
        this.t_init.add(t_init);
    }

    /**
     * Set the ending time
     *
     * @param t_end
     */
    public void set_t_end(double t_end) {
        this.t_end.add(t_end);
    }

    /**
     * Set the optional description of the method
     *
     * @param desc
     */
    public void set_desription(String desc) {
        this.description.add(desc);
    }
}


