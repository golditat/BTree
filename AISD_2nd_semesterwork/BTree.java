public class BTree {

    private int T;

    // Создаем узел
    public class Node {
        public int n;
        int MinDeg = T;
        public int [] key = new int[2 * T - 1];
        public Node [] child = new Node[2 * T];
        public boolean leaf = true;

        public int findKey(int k){

            int idx = 0;
            // Условия выхода из цикла: 1.idx == num, то есть сканировать все
            // 2.idx <num, т.е. найти ключ или больше ключа
            while (idx < n && key[idx] < k)
                ++idx;
            return idx;
        }


        public int  remove(int k){
            int iter = 0;
            int idx = findKey(k);
            if (idx < n && key[idx] == k){ // Найди ключ
                if (leaf) // ключ находится в листовом узле
                   iter +=  removeFromLeaf(idx);
                else // ключ отсутствует в листовом узле
                    iter += removeFromNonLeaf(idx);
            }
            else{
                if (leaf){ // Если узел является листовым узлом, то этот узел не входит в B-дерево
                    return iter;
                }

                // В противном случае удаляемый ключ существует в поддереве с корнем в этом узле

                // Этот флаг указывает, существует ли ключ в поддереве с корнем в последнем дочернем узле узла
                // Когда idx равно num, сравнивается весь узел, и флаг равен true
                boolean flag = idx == n;

                if (child[idx].n < MinDeg) { // Когда дочерний узел узла не заполнен, сначала заполняем его

                    iter += fill(idx);
                }


                // Если последний дочерний узел был объединен, то он должен был быть объединен с предыдущим дочерним узлом, поэтому мы рекурсивно переходим к (idx-1) -ому дочернему узлу.
                // В противном случае мы рекурсивно переходим к (idx) -ому дочернему узлу, который теперь имеет как минимум ключи наименьшей степени
                if (flag && idx > n) {
                    iter += child[idx - 1].remove(k);
                }else{
                    iter += child[idx].remove(k);
                }
            }return iter;
        }

        public int removeFromLeaf(int idx){
            int iter = 0;
            // возвращаемся из idx
            for (int i = idx +1;i < n;++i) {
                iter += 1;
                key[i - 1] = key[i];
            }
            n --;
            return iter;
        }

        public int removeFromNonLeaf(int idx){
            int iter =0;
            int k = key[idx];

            // Если поддерево перед ключом (children [idx]) имеет не менее t ключей
            // Затем находим предшественника key'pred 'в поддереве с корнем в children [idx]
            // Заменить ключ на'pred ', рекурсивно удалить пред в дочерних [idx]
            if (child[idx].n >= MinDeg){
                int pred = getPred(idx);
                key[idx] = pred;
                iter += child[idx].remove(pred);
            }
            // Если у детей [idx] меньше ключей, чем у MinDeg, проверяем дочерние элементы [idx + 1]
            // Если дочерние элементы [idx + 1] имеют хотя бы ключи MinDeg, в поддереве с корнем дочерние элементы [idx + 1]
            // Находим преемника ключа 'ucc' для рекурсивного удаления succ в дочерних элементах [idx + 1]
            else if (child[idx+1].n >= MinDeg){
                int succ = getSucc(idx);
                key[idx] = succ;
                iter += child[idx+1].remove(succ);
            }
            else{
                // Если ключи children [idx] и children [idx + 1] меньше MinDeg
                // затем объединяем ключ и дочерние элементы [idx + 1] в дочерние элементы [idx]
                // Теперь children [idx] содержит ключ 2t-1
                // Освобождаем дочерние элементы [idx + 1], рекурсивно удаляем ключ в children [idx]
                iter += merge(idx);
                iter += child[idx].remove(k);
            }
            return iter;
        }

        public int getPred(int idx){ // Узел-предшественник должен найти крайний правый узел из левого поддерева

            // Продолжаем двигаться к крайнему правому узлу, пока не достигнем листового узла
            Node cur = child[idx];
            while (!cur.leaf)
                cur = cur.child[cur.n];
            return cur.key[cur.n-1];
        }

        public int getSucc(int idx){ // Узел-преемник находится от правого поддерева к левому

            // Продолжаем перемещать крайний левый узел от дочерних [idx + 1], пока не достигнем конечного узла
            Node cur = child[idx+1];
            while (!cur.leaf)
                cur = cur.child[0];
            return cur.key[0];
        }

        // Заполняем дочерние элементы [idx], у которых меньше ключей MinDeg
        public int fill(int idx){
            int iter = 0;
            // Если предыдущий дочерний узел имеет несколько ключей MinDeg-1, заимствовать из них
            if (idx != 0 && child[idx-1].n >= MinDeg)
                iter += borrowFromPrev(idx);
                // Последний дочерний узел имеет несколько ключей MinDeg-1, заимствовать от них
            else if (idx != n && child[idx+1].n >= MinDeg)
                iter += borrowFromNext(idx);
            else{
                // объединить потомков [idx] и его брата
                // Если children [idx] - последний дочерний узел
                // затем объединить его с предыдущим дочерним узлом, иначе объединить его со следующим братом
                if (idx != n)
                    iter += merge(idx);
                else
                    iter += merge(idx-1);
            }
            return iter;
        }

        // Заимствуем ключ у потомков [idx-1] и вставляем его в потомки [idx]
        public int borrowFromPrev(int idx){
            int iter = 0;
            Node children = child[idx];
            Node sibling = child[idx-1];

            // Последний ключ из дочерних [idx-1] переходит к родительскому узлу
            // ключ [idx-1] из недополнения родительского узла вставляется как первый ключ в дочерних [idx]
            // Следовательно, sibling уменьшается на единицу, а children увеличивается на единицу
            for (int i = children.n-1; i >= 0; --i) { // дети [idx] продвигаются вперед
                iter++;
                children.key[i + 1] = children.key[i];
            }
            if (!children.leaf){ // Если дочерний узел [idx] не является листовым, переместите его дочерний узел назад
                for (int i = children.n; i >= 0; --i){
                    iter ++;
                    children.child[i+1] = children.child[i];}
            }

            // Устанавливаем первый ключ дочернего узла на ключи текущего узла [idx-1]
            children.key[0] = key[idx-1];
            if (!children.leaf) { // Устанавливаем последний дочерний узел в качестве первого дочернего узла дочерних элементов [idx]
                children.child[0] = sibling.child[sibling.n];
            }
            // Перемещаем последний ключ брата к последнему из текущего узла
            key[idx-1] = sibling.key[sibling.n-1];
            children.n += 1;
            sibling.n -= 1;
            return iter;
        }

        // Симметричный с заимствованиемFromPrev
        public int borrowFromNext(int idx){
            int iter = 0;
            Node children = child[idx];
            Node sibling = child[idx+1];

            children.key[children.n] = key[idx];

            if (!children.leaf) {
                children.child[children.n + 1] = sibling.child[0];
            }
            key[idx] = sibling.key[0];

            for (int i = 1; i < sibling.n; ++i) {
                iter++;
                sibling.key[i - 1] = sibling.key[i];
            }
            if (!sibling.leaf){
                for (int i= 1; i <= sibling.n;++i){
                    iter++;
                    sibling.child[i-1] = sibling.child[i];}
            }
            children.n += 1;
            sibling.n -= 1;
            return iter;
        }

        // объединить childre [idx + 1] в childre [idx]
        public int merge(int idx){
            int iter = 0;
            Node children = child[idx];
            Node sibling = child[idx+1];

            // Вставляем последний ключ текущего узла в позицию MinDeg-1 дочернего узла
            children.key[MinDeg-1] = key[idx];

            // ключи: children [idx + 1] скопированы в children [idx]
            for (int i =0 ; i< sibling.n; ++i) {
                iter++;
                children.key[i + MinDeg] = sibling.key[i];
            }
            // children: children [idx + 1] скопированы в children [idx]
            if (!children.leaf){
                for (int i = 0;i <= sibling.n; ++i){
                    iter++;
                    children.child[i+MinDeg] = sibling.child[i];}
            }

            // Перемещаем клавиши вперед, а не зазор, вызванный перемещением ключей [idx] к дочерним [idx]
            for (int i = idx+1; i<n; ++i){
                iter++;
                key[i-1] = key[i];}
            // Перемещаем соответствующий дочерний узел вперед
            for (int i = idx+2;i<=n;++i) {
                iter++;
                child[i - 1] = child[i];
            }
            children.n += sibling.n + 1;
            n--;
            return iter;
        }


        public void insertNotFull(int k){

            int i = n -1; // Инициализируем i индексом самого правого значения

            if (leaf){ // Когда это листовой узел
                // Находим, куда нужно вставить новый ключ
                while (i >= 0 && key[i] > k){
                    key[i+1] = key[i]; // клавиши возвращаются
                    i--;
                }
                key[i+1] = k;
                n = n +1;
            }
            else{
                // Находим позицию дочернего узла, который нужно вставить
                while (i >= 0 && key[i] > k)
                    i--;
                if (child[i+1].n == 2*MinDeg - 1){ // Когда дочерний узел заполнен
                    splitChild(i+1,child[i+1]);
                    // После разделения ключ в середине дочернего узла перемещается вверх, а дочерний узел разделяется на два
                    if (key[i+1] < k)
                        i++;
                }
                child[i+1].insertNotFull(k);
            }
        }


        public void splitChild(int i ,Node y){

            // Сначала создаем узел, содержащий ключи MinDeg-1 y
            Node z = new Node();
            z.leaf = y.leaf;
            z.n = MinDeg - 1;

            // Передаем все атрибуты y в z
            for (int j = 0; j < MinDeg-1; j++)
                z.key[j] = y.key[j+MinDeg];
            if (!y.leaf){
                for (int j = 0; j < MinDeg; j++)
                    z.child[j] = y.child[j+MinDeg];
            }
            y.n = MinDeg-1;

            // Вставляем новый дочерний узел в дочерний узел
            for (int j = n; j >= i+1; j--)
                child[j+1] = child[j];
            child[i+1] = z;

            // Перемещаем ключ по y к этому узлу
            for (int j = n-1;j >= i;j--)
                key[j+1] = key[j];
            key[i] = y.key[MinDeg-1];

            n = n + 1;
        }


        public void traverse(){
            int i;
            for (i = 0; i< n; i++){
                if (!leaf)
                    child[i].traverse();
                System.out.printf(" %d",key[i]);
            }

            if (!leaf){
                child[i].traverse();
            }
        }


        public Node search(int k){
            int i = 0;
            while (i < n && k > key[i])
                i++;

            if (key[i] == k)
                return this;
            if (leaf)
                return null;
            return child[i].search(k);
        }

        }

    public BTree(int t) {
        T = t;
        root = new Node();
        root.n = 0;
        root.leaf = true;
    }

    public  Node root;

    // Поиск ключа
    public/*Node*/ int  search1(Node x, int key) {
        int i = 0;
        int iterate = 0;
        if (x == null)
            return iterate;
        for (i = 0; i < x.n; i++) {
            iterate++;
            if (key < x.key[i]) {
                break;
            }
            if (key == x.key[i]) {
                return iterate;
            }
        }
        if (x.leaf) {
            return iterate;
        } else {
            return search1(x.child[i], key);
        }
    }
    private Node  search2(Node x, int key) {
        int i = 0;
        int iterate = 0;
        if (x == null)
            return null;
        for (i = 0; i < x.n; i++) {
            iterate++;
            if (key < x.key[i]) {
                break;
            }
            if (key == x.key[i]) {
                return x;
            }
        }
        if (x.leaf) {
            return null;
        } else {
            return search2(x.child[i], key);
        }
    }

    // Разбиение узла
    private int split(Node x, int pos, Node y) {
        int iter = 0;
        Node z = new Node();
        z.leaf = y.leaf;
        z.n = T - 1;
        for (int j = 0; j < T - 1; j++) {
            iter++;
            z.key[j] = y.key[j + T];
        }
        if (!y.leaf) {
            for (int j = 0; j < T; j++) {
                iter++;
                z.child[j] = y.child[j + T];
            }
        }
        y.n = T - 1;
        for (int j = x.n; j >= pos + 1; j--) {
            iter++;
            x.child[j + 1] = x.child[j];
        }
        x.child[pos + 1] = z;

        for (int j = x.n - 1; j >= pos; j--) {
            iter++;
            x.key[j + 1] = x.key[j];
        }
        x.key[pos] = y.key[T - 1];
        x.n = x.n + 1;
        return iter;
    }

    // Вставка значения
    public int insert(final int key) {
        Node r = root;
        int iter = 0;
        if (r.n == 2 * T - 1) {
            Node s = new Node();
            root = s;
            s.leaf = false;
            s.n = 0;
            s.child[0] = r;
            iter += split(s, 0, r);
            iter += insertValue(s, key);
        } else {
            iter += insertValue(r, key);
        }
        return iter;
    }

    // Вставка узла
    final private int insertValue(Node x, int k) {
        int iter = 0;
        if (x.leaf) {
            int i = 0;
            for (i = x.n - 1; i >= 0 && k < x.key[i]; i--) {
                iter++;
                x.key[i + 1] = x.key[i];
            }
            x.key[i + 1] = k;
            x.n = x.n + 1;
        } else {
            int i = 0;
            for (i = x.n - 1; i >= 0 && k < x.key[i]; i--) {
                iter++;
            }
            i++;
            Node tmp = x.child[i];
            if (tmp.n == 2 * T - 1) {
                split(x, i, tmp);
                if (k > x.key[i]) {
                    i++;
                }
            }
            iter+= insertValue(x.child[i], k);
        }
        return iter;
    }
    public int remove(int k){
        if (root == null){
            System.out.println("The tree is empty");
            return 0;
        }
        int iter = 0;
        iter += root.remove(k);

        if (root.n == 0){ // Если у корневого узла 0 ключей
            // Если у него есть дочерний узел, используйте его первый дочерний узел как новый корневой узел,
            // В противном случае установите корневой узел в ноль
            if (root.leaf) {
                root = null;
            }else{
                root = root.child[0];}
        }
        return iter;
    }
    public void print() {
        print(root);
    }

    // Вывод на экран
    private void print(Node x) {
        assert (x == null);
        for (int i = 0; i < x.n; i++) {
            System.out.print(x.key[i] + " ");
        }
        if (!x.leaf) {
            for (int i = 0; i < x.n + 1; i++) {
                print(x.child[i]);
            }
        }
    }

}
