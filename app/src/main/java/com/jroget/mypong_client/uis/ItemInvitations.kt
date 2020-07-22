import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListAdapter
import android.widget.TextView
import com.jroget.mypong_client.R
import com.jroget.mypong_client.my_classes.auxiliaries.User
import java.io.Serializable
import java.lang.Exception

class ItemInvitations(
    private var list: MutableList<User>,
    private var context: Context
) : BaseAdapter(), ListAdapter, Serializable {

    var accept: ((userKey: String) -> View.OnClickListener)? = null
    var reject: ((userKey: String) -> View.OnClickListener)? = null

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(pos: Int): User {
        return list.get(pos!!)
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View?
        try {
            view = convertView!!
        } catch (e: Exception) {
            var inflater: LayoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;
            view = inflater.inflate(R.layout.item_invitations_layout, null);
        }
        var tvContact: TextView = view!!.findViewById(R.id.txv_username_invitations);
        val user: User = list.get(position!!)
        tvContact.setText(user.username);
        var btnAccept: Button = view!!.findViewById(R.id.btn_accept_invitation);
        btnAccept!!.setOnClickListener(accept!!.invoke(user.key.toString()));
        var btnReject: Button = view!!.findViewById(R.id.btn_reject_invitation);
        btnReject!!.setOnClickListener(reject!!.invoke(user.key.toString()));
        return view;
    }
}