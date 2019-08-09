package opt.data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Node {
    protected long id;
    protected HashSet<Long> in_links = new HashSet<>();
    protected HashSet<Long> out_links = new HashSet<>();

    /////////////////////////////////////
    // construction
    /////////////////////////////////////

    protected Node(jaxb.Node jnode){
        this.id = jnode.getId();
    }

    protected Node(long id){
        this.id = id;
    }

//    protected Node deep_copy(){
//        Node new_node = new Node(this.id);
//        new_node.in_links = (HashSet<Long>) in_links.clone();
//        new_node.out_links = (HashSet<Long>) out_links.clone();
//        return new_node;
//    }

    /////////////////////////////////////
    // override
    /////////////////////////////////////

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id == node.id &&
                in_links.equals(node.in_links) &&
                out_links.equals(node.out_links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, in_links, out_links);
    }

    @Override
    public String toString() {
        return String.format("%d",id);
    }


}
