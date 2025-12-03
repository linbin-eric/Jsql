package cc.jfire.jsql.test.vo;

public class AbstractUser
{
    protected int     age;
    protected Boolean B11 = false;
    protected Double  D11 = 6.32d;


    public int getAge()
    {
        return age;
    }

    public void setAge(int age)
    {
        this.age = age;
    }

    public Boolean getB11()
    {
        return B11;
    }

    public void setB11(Boolean b11)
    {
        B11 = b11;
    }

    public Double getD11()
    {
        return D11;
    }

    public void setD11(Double d11)
    {
        D11 = d11;
    }
}
