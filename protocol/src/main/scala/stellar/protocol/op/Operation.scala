package stellar.protocol.op

trait Operation {

  def xdrEncode: org.stellar.xdr.Operation

}
