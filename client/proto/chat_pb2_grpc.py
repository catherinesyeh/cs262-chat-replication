# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
"""Client and server classes corresponding to protobuf-defined services."""
import grpc
import warnings

from . import chat_pb2 as chat__pb2

GRPC_GENERATED_VERSION = '1.71.0'
GRPC_VERSION = grpc.__version__
_version_not_supported = False

try:
    from grpc._utilities import first_version_is_lower
    _version_not_supported = first_version_is_lower(
        GRPC_VERSION, GRPC_GENERATED_VERSION)
except ImportError:
    _version_not_supported = True

if _version_not_supported:
    raise RuntimeError(
        f'The grpc package installed is at version {GRPC_VERSION},'
        + f' but the generated code in chat_pb2_grpc.py depends on'
        + f' grpcio>={GRPC_GENERATED_VERSION}.'
        + f' Please upgrade your grpc module to grpcio>={GRPC_GENERATED_VERSION}'
        + f' or downgrade your generated code using grpcio-tools<={GRPC_VERSION}.'
    )


class ChatServiceStub(object):
    """Missing associated documentation comment in .proto file."""

    def __init__(self, channel):
        """Constructor.

        Args:
            channel: A grpc.Channel.
        """
        self.AccountLookup = channel.unary_unary(
            '/edu.harvard.ChatService/AccountLookup',
            request_serializer=chat__pb2.AccountLookupRequest.SerializeToString,
            response_deserializer=chat__pb2.AccountLookupResponse.FromString,
            _registered_method=True)
        self.Login = channel.unary_unary(
            '/edu.harvard.ChatService/Login',
            request_serializer=chat__pb2.LoginCreateRequest.SerializeToString,
            response_deserializer=chat__pb2.LoginCreateResponse.FromString,
            _registered_method=True)
        self.CreateAccount = channel.unary_unary(
            '/edu.harvard.ChatService/CreateAccount',
            request_serializer=chat__pb2.LoginCreateRequest.SerializeToString,
            response_deserializer=chat__pb2.LoginCreateResponse.FromString,
            _registered_method=True)
        self.ListAccounts = channel.unary_unary(
            '/edu.harvard.ChatService/ListAccounts',
            request_serializer=chat__pb2.ListAccountsRequest.SerializeToString,
            response_deserializer=chat__pb2.ListAccountsResponse.FromString,
            _registered_method=True)
        self.SendMessage = channel.unary_unary(
            '/edu.harvard.ChatService/SendMessage',
            request_serializer=chat__pb2.SendMessageRequest.SerializeToString,
            response_deserializer=chat__pb2.SendMessageResponse.FromString,
            _registered_method=True)
        self.RequestMessages = channel.unary_unary(
            '/edu.harvard.ChatService/RequestMessages',
            request_serializer=chat__pb2.RequestMessagesRequest.SerializeToString,
            response_deserializer=chat__pb2.RequestMessagesResponse.FromString,
            _registered_method=True)
        self.DeleteMessages = channel.unary_unary(
            '/edu.harvard.ChatService/DeleteMessages',
            request_serializer=chat__pb2.DeleteMessagesRequest.SerializeToString,
            response_deserializer=chat__pb2.Empty.FromString,
            _registered_method=True)
        self.DeleteAccount = channel.unary_unary(
            '/edu.harvard.ChatService/DeleteAccount',
            request_serializer=chat__pb2.DeleteAccountRequest.SerializeToString,
            response_deserializer=chat__pb2.Empty.FromString,
            _registered_method=True)
        self.GetOtherAvailableReplicas = channel.unary_unary(
            '/edu.harvard.ChatService/GetOtherAvailableReplicas',
            request_serializer=chat__pb2.Empty.SerializeToString,
            response_deserializer=chat__pb2.AvailableReplicas.FromString,
            _registered_method=True)


class ChatServiceServicer(object):
    """Missing associated documentation comment in .proto file."""

    def AccountLookup(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def Login(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def CreateAccount(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def ListAccounts(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def SendMessage(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def RequestMessages(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def DeleteMessages(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def DeleteAccount(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def GetOtherAvailableReplicas(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')


def add_ChatServiceServicer_to_server(servicer, server):
    rpc_method_handlers = {
        'AccountLookup': grpc.unary_unary_rpc_method_handler(
            servicer.AccountLookup,
            request_deserializer=chat__pb2.AccountLookupRequest.FromString,
            response_serializer=chat__pb2.AccountLookupResponse.SerializeToString,
        ),
        'Login': grpc.unary_unary_rpc_method_handler(
            servicer.Login,
            request_deserializer=chat__pb2.LoginCreateRequest.FromString,
            response_serializer=chat__pb2.LoginCreateResponse.SerializeToString,
        ),
        'CreateAccount': grpc.unary_unary_rpc_method_handler(
            servicer.CreateAccount,
            request_deserializer=chat__pb2.LoginCreateRequest.FromString,
            response_serializer=chat__pb2.LoginCreateResponse.SerializeToString,
        ),
        'ListAccounts': grpc.unary_unary_rpc_method_handler(
            servicer.ListAccounts,
            request_deserializer=chat__pb2.ListAccountsRequest.FromString,
            response_serializer=chat__pb2.ListAccountsResponse.SerializeToString,
        ),
        'SendMessage': grpc.unary_unary_rpc_method_handler(
            servicer.SendMessage,
            request_deserializer=chat__pb2.SendMessageRequest.FromString,
            response_serializer=chat__pb2.SendMessageResponse.SerializeToString,
        ),
        'RequestMessages': grpc.unary_unary_rpc_method_handler(
            servicer.RequestMessages,
            request_deserializer=chat__pb2.RequestMessagesRequest.FromString,
            response_serializer=chat__pb2.RequestMessagesResponse.SerializeToString,
        ),
        'DeleteMessages': grpc.unary_unary_rpc_method_handler(
            servicer.DeleteMessages,
            request_deserializer=chat__pb2.DeleteMessagesRequest.FromString,
            response_serializer=chat__pb2.Empty.SerializeToString,
        ),
        'DeleteAccount': grpc.unary_unary_rpc_method_handler(
            servicer.DeleteAccount,
            request_deserializer=chat__pb2.DeleteAccountRequest.FromString,
            response_serializer=chat__pb2.Empty.SerializeToString,
        ),
        'GetOtherAvailableReplicas': grpc.unary_unary_rpc_method_handler(
            servicer.GetOtherAvailableReplicas,
            request_deserializer=chat__pb2.Empty.FromString,
            response_serializer=chat__pb2.AvailableReplicas.SerializeToString,
        ),
    }
    generic_handler = grpc.method_handlers_generic_handler(
        'edu.harvard.ChatService', rpc_method_handlers)
    server.add_generic_rpc_handlers((generic_handler,))
    server.add_registered_method_handlers(
        'edu.harvard.ChatService', rpc_method_handlers)

 # This class is part of an EXPERIMENTAL API.


class ChatService(object):
    """Missing associated documentation comment in .proto file."""

    @staticmethod
    def AccountLookup(request,
                      target,
                      options=(),
                      channel_credentials=None,
                      call_credentials=None,
                      insecure=False,
                      compression=None,
                      wait_for_ready=None,
                      timeout=None,
                      metadata=None):
        return grpc.experimental.unary_unary(
            request,
            target,
            '/edu.harvard.ChatService/AccountLookup',
            chat__pb2.AccountLookupRequest.SerializeToString,
            chat__pb2.AccountLookupResponse.FromString,
            options,
            channel_credentials,
            insecure,
            call_credentials,
            compression,
            wait_for_ready,
            timeout,
            metadata,
            _registered_method=True)

    @staticmethod
    def Login(request,
              target,
              options=(),
              channel_credentials=None,
              call_credentials=None,
              insecure=False,
              compression=None,
              wait_for_ready=None,
              timeout=None,
              metadata=None):
        return grpc.experimental.unary_unary(
            request,
            target,
            '/edu.harvard.ChatService/Login',
            chat__pb2.LoginCreateRequest.SerializeToString,
            chat__pb2.LoginCreateResponse.FromString,
            options,
            channel_credentials,
            insecure,
            call_credentials,
            compression,
            wait_for_ready,
            timeout,
            metadata,
            _registered_method=True)

    @staticmethod
    def CreateAccount(request,
                      target,
                      options=(),
                      channel_credentials=None,
                      call_credentials=None,
                      insecure=False,
                      compression=None,
                      wait_for_ready=None,
                      timeout=None,
                      metadata=None):
        return grpc.experimental.unary_unary(
            request,
            target,
            '/edu.harvard.ChatService/CreateAccount',
            chat__pb2.LoginCreateRequest.SerializeToString,
            chat__pb2.LoginCreateResponse.FromString,
            options,
            channel_credentials,
            insecure,
            call_credentials,
            compression,
            wait_for_ready,
            timeout,
            metadata,
            _registered_method=True)

    @staticmethod
    def ListAccounts(request,
                     target,
                     options=(),
                     channel_credentials=None,
                     call_credentials=None,
                     insecure=False,
                     compression=None,
                     wait_for_ready=None,
                     timeout=None,
                     metadata=None):
        return grpc.experimental.unary_unary(
            request,
            target,
            '/edu.harvard.ChatService/ListAccounts',
            chat__pb2.ListAccountsRequest.SerializeToString,
            chat__pb2.ListAccountsResponse.FromString,
            options,
            channel_credentials,
            insecure,
            call_credentials,
            compression,
            wait_for_ready,
            timeout,
            metadata,
            _registered_method=True)

    @staticmethod
    def SendMessage(request,
                    target,
                    options=(),
                    channel_credentials=None,
                    call_credentials=None,
                    insecure=False,
                    compression=None,
                    wait_for_ready=None,
                    timeout=None,
                    metadata=None):
        return grpc.experimental.unary_unary(
            request,
            target,
            '/edu.harvard.ChatService/SendMessage',
            chat__pb2.SendMessageRequest.SerializeToString,
            chat__pb2.SendMessageResponse.FromString,
            options,
            channel_credentials,
            insecure,
            call_credentials,
            compression,
            wait_for_ready,
            timeout,
            metadata,
            _registered_method=True)

    @staticmethod
    def RequestMessages(request,
                        target,
                        options=(),
                        channel_credentials=None,
                        call_credentials=None,
                        insecure=False,
                        compression=None,
                        wait_for_ready=None,
                        timeout=None,
                        metadata=None):
        return grpc.experimental.unary_unary(
            request,
            target,
            '/edu.harvard.ChatService/RequestMessages',
            chat__pb2.RequestMessagesRequest.SerializeToString,
            chat__pb2.RequestMessagesResponse.FromString,
            options,
            channel_credentials,
            insecure,
            call_credentials,
            compression,
            wait_for_ready,
            timeout,
            metadata,
            _registered_method=True)

    @staticmethod
    def DeleteMessages(request,
                       target,
                       options=(),
                       channel_credentials=None,
                       call_credentials=None,
                       insecure=False,
                       compression=None,
                       wait_for_ready=None,
                       timeout=None,
                       metadata=None):
        return grpc.experimental.unary_unary(
            request,
            target,
            '/edu.harvard.ChatService/DeleteMessages',
            chat__pb2.DeleteMessagesRequest.SerializeToString,
            chat__pb2.Empty.FromString,
            options,
            channel_credentials,
            insecure,
            call_credentials,
            compression,
            wait_for_ready,
            timeout,
            metadata,
            _registered_method=True)

    @staticmethod
    def DeleteAccount(request,
                      target,
                      options=(),
                      channel_credentials=None,
                      call_credentials=None,
                      insecure=False,
                      compression=None,
                      wait_for_ready=None,
                      timeout=None,
                      metadata=None):
        return grpc.experimental.unary_unary(
            request,
            target,
            '/edu.harvard.ChatService/DeleteAccount',
            chat__pb2.DeleteAccountRequest.SerializeToString,
            chat__pb2.Empty.FromString,
            options,
            channel_credentials,
            insecure,
            call_credentials,
            compression,
            wait_for_ready,
            timeout,
            metadata,
            _registered_method=True)

    @staticmethod
    def GetOtherAvailableReplicas(request,
                                  target,
                                  options=(),
                                  channel_credentials=None,
                                  call_credentials=None,
                                  insecure=False,
                                  compression=None,
                                  wait_for_ready=None,
                                  timeout=None,
                                  metadata=None):
        return grpc.experimental.unary_unary(
            request,
            target,
            '/edu.harvard.ChatService/GetOtherAvailableReplicas',
            chat__pb2.Empty.SerializeToString,
            chat__pb2.AvailableReplicas.FromString,
            options,
            channel_credentials,
            insecure,
            call_credentials,
            compression,
            wait_for_ready,
            timeout,
            metadata,
            _registered_method=True)
