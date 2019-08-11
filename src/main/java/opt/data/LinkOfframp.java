package opt.data;

import jaxb.Roadparam;
import profiles.Profile1D;

public class LinkOfframp extends AbstractLink {

    /////////////////////////////////////
    // construction
    /////////////////////////////////////

    public LinkOfframp(jaxb.Link link, Roadparam rp) {
        super(link, AbstractLink.Type.offramp, rp);
    }

    public LinkOfframp(Long id, Long start_node_id, Long end_node_id, Integer full_lanes, Integer managed_lanes ,Float length, Float capacity_vphpl, Float jam_density_vpkpl, Float ff_speed_kph, Segment mysegment) {
        super(id, AbstractLink.Type.offramp, start_node_id, end_node_id, full_lanes, managed_lanes, 0, length, capacity_vphpl, jam_density_vpkpl, ff_speed_kph, mysegment);
    }

    /////////////////////////////////////
    // up and dn segment
    /////////////////////////////////////

    @Override
    public Segment get_up_segment(){
        return up_link.get_up_segment();
    }

    /////////////////////////////////////
    // lanes
    /////////////////////////////////////

    @Override
    public void set_aux_lanes(int x) throws Exception {
        throw new Exception("Attempted to set aux lanes on an offramp");
    }

    /////////////////////////////////////
    // demands and splits
    /////////////////////////////////////

    @Override
    public void set_split(Long comm_id, Profile1D profile) throws Exception {
        this.splits.put(comm_id,profile);
    }

    /////////////////////////////////////
    // insert
    /////////////////////////////////////

    @Override
    public Segment insert_dn_segment() {

        // create new dnstream link
        LinkConnector new_link = (LinkConnector) create_dn_FwyOrConnLink(Type.connector);

        // wrap in a segment
        Segment new_segment = create_segment(new_link);

        // connect dnstream segment to new node
        Segment dn_segment = get_dn_segment();
        if(dn_segment!=null) {
            connect_segments_upstr_node_to(dn_segment, new_link.end_node_id);
            new_link.dn_link = dn_segment.fwy;
            dn_segment.fwy.up_link = new_link;
        }

        return new_segment;
    }

    @Override
    public Segment insert_up_segment() {
        return null;
    }

}
