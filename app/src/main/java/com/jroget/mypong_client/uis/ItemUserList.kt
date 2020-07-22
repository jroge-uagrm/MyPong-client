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

class ItemUserList(
    private var list: MutableList<User>,
    private var context: Context
) : BaseAdapter(), ListAdapter, Serializable {

    var invite: ((pos: Int) -> View.OnClickListener)? = null

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
            view = inflater.inflate(R.layout.item_user_list_layout, null);
        }
        var tvContact: TextView = view!!.findViewById(R.id.txv_username_user_list);
        val user: User = list.get(position!!)
        tvContact.setText(user.username);
        var btnInvite: Button = view!!.findViewById(R.id.btn_status_user_list);
        btnInvite!!.setOnClickListener(invite!!.invoke(position));
        if (user.matchId!!.isNotEmpty()) {
            btnInvite.setBackgroundColor(Color.YELLOW)
            btnInvite.isEnabled = false
            btnInvite.text = "In game"
        }
        return view;
    }
}