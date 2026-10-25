package Server.Services;

import java.util.List;
import java.util.Random;

import Server.Encounter.Area;
import Server.Encounter.AreaForm;
import Server.Repository.Repository;

// Areas: a name, a level range and the forms that can appear there (with weights).
// The Master rolls an area to spawn a random Pokemon of it.
public class AreaServices {

    private Repository repository;

    private final Random random = new Random();

    public AreaServices(Repository repository) {
        setRepository(repository);
    }

    //============GETTERS=============

    public Repository getRepository() {
        return repository;
    }

    //============SETTERS=============

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    //============AREAS=============

    // [{"id":1,"name":"Route 1","min":2,"max":5,"forms":[{"id":9,"formId":16,"weight":3.0}]}]
    public String listJson() {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (Area area : repository.findAllAreas()) {
            if (!first) json.append(",");
            first = false;
            json.append("{\"id\":").append(area.getId())
                .append(",\"name\":\"").append(area.getName().replace("\\", "").replace("\"", ""))
                .append("\",\"min\":").append(area.getMinLevel())
                .append(",\"max\":").append(area.getMaxLevel())
                .append(",\"forms\":[");
            boolean firstForm = true;
            for (AreaForm form : repository.findAreaFormsOf(area.getId())) {
                if (!firstForm) json.append(",");
                firstForm = false;
                json.append("{\"id\":").append(form.getId())
                    .append(",\"formId\":").append(form.getFormId())
                    .append(",\"weight\":").append(form.getWeight()).append("}");
            }
            json.append("]}");
        }
        return json.append("]").toString();
    }

    // Creates the area (id = 0) or edits it. Returns an error message, or null when it worked.
    public synchronized String save(int id, String name, int min, int max) {
        if (name == null || name.isBlank()) return "Name required";
        if (min < 1 || max > 100 || min > max) return "Levels must be 1-100 and min <= max";
        if (id == 0) {
            int next = 1;
            for (Area area : repository.findAllAreas()) next = Math.max(next, area.getId() + 1);
            repository.createArea(new Area(next, name.trim(), min, max));
        } else {
            if (repository.findAreaById(id) == null) return "Area not found";
            repository.updateArea(new Area(id, name.trim(), min, max));
        }
        return null;
    }

    public synchronized void delete(int id) {
        repository.deleteAreaFormsOf(id);
        repository.deleteArea(id);
    }

    //============FORMS OF AN AREA=============

    public String setForm(int areaId, int formId, double weight) {
        if (repository.findAreaById(areaId) == null) return "Area not found";
        if (repository.findPokemonFormById(formId) == null) return "Form not found";
        if (!(weight > 0)) return "Weight must be above 0";
        repository.saveAreaForm(areaId, formId, weight);
        return null;
    }

    public void removeForm(int rowId) {
        repository.deleteAreaForm(rowId);
    }

    //============ROLL=============

    // {formId, level}: a form picked by weight and a level between the area's limits; null = empty area
    public int[] roll(int areaId) {
        Area area = repository.findAreaById(areaId);
        List<AreaForm> forms = repository.findAreaFormsOf(areaId);
        if (area == null || forms.isEmpty()) return null;

        double total = 0;
        for (AreaForm form : forms) total += form.getWeight();

        double pick = random.nextDouble() * total;
        AreaForm chosen = forms.get(forms.size() - 1);
        for (AreaForm form : forms) {
            pick -= form.getWeight();
            if (pick < 0) { chosen = form; break; }
        }

        int level = area.getMinLevel() + random.nextInt(area.getMaxLevel() - area.getMinLevel() + 1);
        return new int[] { chosen.getFormId(), level };
    }

}
